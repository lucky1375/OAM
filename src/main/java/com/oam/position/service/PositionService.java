package com.oam.position.service;

import com.oam.common.security.SecurityUtil;
import com.oam.department.entity.Department;
import com.oam.department.repository.DepartmentRepository;
import com.oam.position.dto.PositionCreateRequest;
import com.oam.position.dto.PositionDto;
import com.oam.position.dto.PositionUpdateRequest;
import com.oam.position.entity.Position;
import com.oam.position.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    public PositionService(PositionRepository positionRepository,
                           DepartmentRepository departmentRepository) {
        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
    }

    private Map<Long, String> deptNameCache() {
        List<Department> all = departmentRepository.findAll();
        Map<Long, String> map = new HashMap<>();
        for (Department d : all) map.put(d.getId(), d.getName());
        return map;
    }

    public List<PositionDto> list(Long departmentId, String keyword) {
        List<Position> list;
        if (departmentId != null) {
            list = positionRepository.findByDepartmentIdOrderByIdAsc(departmentId);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            list = positionRepository.findByNameContaining(keyword.trim());
        } else {
            list = positionRepository.findAllByOrderByIdAsc();
        }
        Map<Long, String> cache = deptNameCache();
        return list.stream().map(p -> PositionDto.from(p, cache.get(p.getDepartmentId()))).collect(Collectors.toList());
    }

    @Transactional
    public PositionDto create(PositionCreateRequest request) {
        Long deptId = request.getDepartmentId();
        if (deptId != null && !departmentRepository.existsById(deptId)) {
            throw new IllegalArgumentException("归属部门不存在");
        }
        if (positionRepository.existsByDepartmentIdAndName(deptId, request.getName())) {
            throw new IllegalArgumentException("该部门下已存在同名岗位");
        }
        Position p = new Position();
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setDepartmentId(deptId);
        p.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        p.setCreatedBy(SecurityUtil.currentUsername());
        Position saved = positionRepository.save(p);
        Map<Long, String> cache = deptNameCache();
        return PositionDto.from(saved, cache.get(saved.getDepartmentId()));
    }

    @Transactional
    public PositionDto update(Long id, PositionUpdateRequest request) {
        Position p = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        Long deptId = request.getDepartmentId();
        if (deptId != null && !departmentRepository.existsById(deptId)) {
            throw new IllegalArgumentException("归属部门不存在");
        }
        Long excludeId = id;
        if (positionRepository.existsByDepartmentIdAndNameAndIdNot(deptId, request.getName(), excludeId)) {
            throw new IllegalArgumentException("该部门下已存在同名岗位");
        }
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setDepartmentId(deptId);
        p.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        p.setUpdatedBy(SecurityUtil.currentUsername());
        p.setUpdatedAt(LocalDateTime.now());
        Position saved = positionRepository.save(p);
        Map<Long, String> cache = deptNameCache();
        return PositionDto.from(saved, cache.get(saved.getDepartmentId()));
    }

    @Transactional
    public void delete(Long id) {
        Position p = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        positionRepository.delete(p);
    }
}
