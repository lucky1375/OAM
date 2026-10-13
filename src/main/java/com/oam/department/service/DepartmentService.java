package com.oam.department.service;

import com.oam.common.security.SecurityUtil;
import com.oam.department.dto.DepartmentCreateRequest;
import com.oam.department.dto.DepartmentDto;
import com.oam.department.dto.DepartmentUpdateRequest;
import com.oam.department.entity.Department;
import com.oam.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /** 整棵树 */
    public List<DepartmentDto> tree() {
        List<Department> all = departmentRepository.findAllByOrderByLevelAscSortAscIdAsc();
        return buildTree(all, null, null);
    }

    /** 平铺列表（含完整路径名，用于级联选择器的扁平化数据源） */
    public List<DepartmentDto> flatList() {
        List<Department> all = departmentRepository.findAllByOrderByLevelAscSortAscIdAsc();
        Map<Long, Department> byId = new HashMap<>();
        for (Department d : all) byId.put(d.getId(), d);
        return all.stream().map(d -> toFlatDto(d, byId)).collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto create(DepartmentCreateRequest request) {
        Long parentId = request.getParentId();
        Integer level = 0;
        if (parentId != null) {
            Department parent = requireExists(parentId);
            level = parent.getLevel() + 1;
        }
        // 同级 name 唯一
        if (departmentRepository.existsByParentIdAndName(parentId, request.getName())) {
            throw new IllegalArgumentException("同级下已存在同名部门");
        }
        Department dept = new Department();
        dept.setName(request.getName());
        dept.setParentId(parentId);
        dept.setLevel(level);
        dept.setSort(request.getSort() == null ? 0 : request.getSort());
        dept.setLeaderName(request.getLeaderName());
        dept.setPhone(request.getPhone());
        dept.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        dept.setRemark(request.getRemark());
        dept.setCreatedBy(SecurityUtil.currentUsername());
        return toFlatDto(departmentRepository.save(dept), snapshotAll());
    }

    @Transactional
    public DepartmentDto update(Long id, DepartmentUpdateRequest request) {
        Department dept = requireExists(id);

        // 同级 name 唯一（排除自身）
        Long newParentId = request.getParentId();
        Long excludeId = id; // 显式变量，避免 lambda 捕获问题
        if (departmentRepository.existsByParentIdAndNameAndIdNot(newParentId, request.getName(), excludeId)) {
            throw new IllegalArgumentException("同级下已存在同名部门");
        }

        // 防循环：不能把节点移到自己或自己的后代下
        if (newParentId != null && (newParentId.equals(id) || isAncestorOf(id, newParentId))) {
            throw new IllegalArgumentException("不能将部门移动到自身或其子孙下");
        }

        Integer newLevel;
        if (Objects.equals(dept.getParentId(), newParentId)) {
            // 父级不变 → level 不变
            newLevel = dept.getLevel();
        } else {
            newLevel = newParentId == null ? 0 : requireExists(newParentId).getLevel() + 1;
        }
        int levelDiff = newLevel - dept.getLevel();

        dept.setName(request.getName());
        dept.setParentId(newParentId);
        dept.setLevel(newLevel);
        dept.setSort(request.getSort() == null ? 0 : request.getSort());
        dept.setLeaderName(request.getLeaderName());
        dept.setPhone(request.getPhone());
        dept.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        dept.setRemark(request.getRemark());
        dept.setUpdatedBy(SecurityUtil.currentUsername());
        dept.setUpdatedAt(LocalDateTime.now());
        dept = departmentRepository.save(dept);

        // 同步更新所有后代的 level
        if (levelDiff != 0) {
            List<Department> descendants = collectDescendants(id);
            for (Department child : descendants) {
                child.setLevel(child.getLevel() + levelDiff);
            }
            departmentRepository.saveAll(descendants);
        }

        return toFlatDto(dept, snapshotAll());
    }

    @Transactional
    public void delete(Long id) {
        Department dept = requireExists(id);
        if (departmentRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("存在子部门，无法删除");
        }
        departmentRepository.delete(dept);
    }

    // ========== 内部工具 ==========

    private Department requireExists(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
    }

    /** descendantId 是否是 ancestorId 的后代（含自身） */
    private boolean isAncestorOf(Long ancestorId, Long descendantId) {
        Long parentId = descendantId;
        while (parentId != null) {
            if (parentId.equals(ancestorId)) return true;
            Department p = departmentRepository.findById(parentId).orElse(null);
            if (p == null) break;
            parentId = p.getParentId();
        }
        return false;
    }

    /** 收集某节点所有后代（不含自身） */
    private List<Department> collectDescendants(Long id) {
        List<Department> all = departmentRepository.findAll();
        Map<Long, List<Department>> childrenMap = new HashMap<>();
        for (Department d : all) {
            childrenMap.computeIfAbsent(d.getParentId() == null ? -1L : d.getParentId(), k -> new ArrayList<>()).add(d);
        }
        List<Department> result = new ArrayList<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(id);
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            List<Department> kids = childrenMap.getOrDefault(cur, Collections.emptyList());
            for (Department kid : kids) {
                result.add(kid);
                queue.add(kid.getId());
            }
        }
        return result;
    }

    private Map<Long, Department> snapshotAll() {
        Map<Long, Department> byId = new HashMap<>();
        for (Department d : departmentRepository.findAll()) byId.put(d.getId(), d);
        return byId;
    }

    private DepartmentDto toFlatDto(Department d, Map<Long, Department> byId) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setParentId(d.getParentId());
        dto.setLevel(d.getLevel());
        dto.setSort(d.getSort());
        dto.setLeaderName(d.getLeaderName());
        dto.setPhone(d.getPhone());
        dto.setEnabled(d.getEnabled());
        dto.setRemark(d.getRemark());
        dto.setCreatedAt(DepartmentDto.fmt(d.getCreatedAt()));
        dto.setCreatedBy(d.getCreatedBy());
        dto.setUpdatedBy(d.getUpdatedBy());
        dto.setUpdatedAt(DepartmentDto.fmt(d.getUpdatedAt()));
        dto.setPathNames(buildPath(d, byId));
        return dto;
    }

    /** 从当前节点向上回溯完整路径名 */
    private String buildPath(Department d, Map<Long, Department> byId) {
        Deque<String> parts = new ArrayDeque<>();
        Department cur = d;
        while (cur != null) {
            parts.addFirst(cur.getName());
            Long pid = cur.getParentId();
            cur = pid == null ? null : byId.get(pid);
        }
        return String.join(" / ", parts);
    }

    /** 扁平记录按 parentId 递归组树 */
    private List<DepartmentDto> buildTree(List<Department> all, Long parentId, Map<Long, Department> snapshot) {
        Map<Long, Department> byId = snapshot != null ? snapshot : snapshotAll();
        return all.stream()
                .filter(d -> Objects.equals(d.getParentId(), parentId))
                .sorted(Comparator.comparing(Department::getSort).thenComparing(Department::getId))
                .map(d -> {
                    DepartmentDto dto = toFlatDto(d, byId);
                    dto.setChildren(buildTree(all, d.getId(), byId));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
