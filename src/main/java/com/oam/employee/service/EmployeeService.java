package com.oam.employee.service;

import com.oam.common.security.SecurityUtil;
import com.oam.department.entity.Department;
import com.oam.department.repository.DepartmentRepository;
import com.oam.employee.dto.EmployeeCreateRequest;
import com.oam.employee.dto.EmployeeDto;
import com.oam.employee.dto.EmployeeUpdateRequest;
import com.oam.employee.entity.Employee;
import com.oam.employee.repository.EmployeeRepository;
import com.oam.position.entity.Position;
import com.oam.position.repository.PositionRepository;
import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           PositionRepository positionRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Map<Long, String> deptNameCache() {
        Map<Long, String> map = new HashMap<>();
        for (Department d : departmentRepository.findAll()) map.put(d.getId(), d.getName());
        return map;
    }
    private Map<Long, String> posNameCache() {
        Map<Long, String> map = new HashMap<>();
        for (Position p : positionRepository.findAll()) map.put(p.getId(), p.getName());
        return map;
    }
    private Map<Long, User> userCache() {
        Map<Long, User> map = new HashMap<>();
        for (User u : userRepository.findAll()) map.put(u.getId(), u);
        return map;
    }

    public List<EmployeeDto> list(Long departmentId, String keyword) {
        List<Employee> list;
        if (departmentId != null) {
            list = employeeRepository.findByDepartmentId(departmentId);
        } else {
            list = employeeRepository.findAll();
        }
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (!kw.isEmpty()) {
            list = list.stream().filter(e ->
                    (e.getName() != null && e.getName().toLowerCase().contains(kw))
                            || (e.getPhone() != null && e.getPhone().toLowerCase().contains(kw))
                            || (e.getEmployeeNo() != null && e.getEmployeeNo().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }
        Map<Long, String> dc = deptNameCache();
        Map<Long, String> pc = posNameCache();
        Map<Long, User> uc = userCache();
        return list.stream().map(e -> EmployeeDto.from(e, dc.get(e.getDepartmentId()), pc.get(e.getPositionId()), uc.get(e.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 新增员工 = Employee + 自动创建 User 账号（默认 USER 角色）。
     */
    @Transactional
    public EmployeeDto create(EmployeeCreateRequest request) {
        // 校验部门/岗位
        if (request.getDepartmentId() != null && !departmentRepository.existsById(request.getDepartmentId())) {
            throw new IllegalArgumentException("归属部门不存在");
        }
        if (request.getPositionId() != null && !positionRepository.existsById(request.getPositionId())) {
            throw new IllegalArgumentException("岗位不存在");
        }
        if (request.getEmployeeNo() != null && !request.getEmployeeNo().isEmpty()
                && employeeRepository.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new IllegalArgumentException("工号已存在");
        }

        // 1) 自动创建 User（默认 USER 角色）
        String username = genUsername(request);
        String password = request.getInitialPassword();
        if (password == null || password.isEmpty()) {
            password = genInitialPassword(request.getPhone());
        }
        if (password.length() < 6) {
            password = "123456";
        }
        User user = new User();
        user.setUsername(username);
        user.setNickname(request.getName());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER); // 普通用户，默认角色
        user.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCreatedBy(SecurityUtil.currentUsername());
        user = userRepository.save(user);

        // 2) Employee
        Employee e = new Employee();
        e.setName(request.getName());
        e.setEmployeeNo(request.getEmployeeNo());
        e.setGender(request.getGender());
        e.setPhone(request.getPhone());
        e.setEmail(request.getEmail());
        e.setDepartmentId(request.getDepartmentId());
        e.setPositionId(request.getPositionId());
        e.setUserId(user.getId());
        e.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        e.setCreatedBy(SecurityUtil.currentUsername());
        e = employeeRepository.save(e);

        Map<Long, String> dc = deptNameCache();
        Map<Long, String> pc = posNameCache();
        return EmployeeDto.from(e, dc.get(e.getDepartmentId()), pc.get(e.getPositionId()), user);
    }

    @Transactional
    public EmployeeDto update(Long id, EmployeeUpdateRequest request) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        if (request.getDepartmentId() != null && !departmentRepository.existsById(request.getDepartmentId())) {
            throw new IllegalArgumentException("归属部门不存在");
        }
        if (request.getPositionId() != null && !positionRepository.existsById(request.getPositionId())) {
            throw new IllegalArgumentException("岗位不存在");
        }
        e.setName(request.getName());
        e.setEmployeeNo(request.getEmployeeNo());
        e.setGender(request.getGender());
        e.setPhone(request.getPhone());
        e.setEmail(request.getEmail());
        e.setDepartmentId(request.getDepartmentId());
        e.setPositionId(request.getPositionId());
        e.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        e.setUpdatedBy(SecurityUtil.currentUsername());
        e.setUpdatedAt(LocalDateTime.now());
        e = employeeRepository.save(e);

        // 同步更新 User 账号的昵称/手机/邮箱/启停（方便管理）
        if (e.getUserId() != null) {
            User u = userRepository.findById(e.getUserId()).orElse(null);
            if (u != null) {
                u.setNickname(e.getName());
                u.setPhone(e.getPhone());
                u.setEmail(e.getEmail());
                u.setEnabled(e.getEnabled());
                u.setUpdatedBy(SecurityUtil.currentUsername());
                u.setUpdatedAt(LocalDateTime.now());
                userRepository.save(u);
            }
        }

        Map<Long, String> dc = deptNameCache();
        Map<Long, String> pc = posNameCache();
        Map<Long, User> uc = userCache();
        return EmployeeDto.from(e, dc.get(e.getDepartmentId()), pc.get(e.getPositionId()), uc.get(e.getUserId()));
    }

    @Transactional
    public void delete(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        // 同时删除关联的普通账号，但禁止删除关联 ADMIN 的员工
        if (e.getUserId() != null) {
            User u = userRepository.findById(e.getUserId()).orElse(null);
            if (u != null) {
                if (u.getRole() == Role.ADMIN) {
                    throw new IllegalArgumentException("员工关联管理员账号，不能删除");
                }
                userRepository.delete(u);
            }
        }
        employeeRepository.delete(e);
    }

    // ========== 账号自动生成规则 ==========

    /** 生成 username：employeeNo 存在用 employee_no，否则用拼音占位 eno + id */
    private String genUsername(EmployeeCreateRequest req) {
        if (req.getUsername() != null && !req.getUsername().isEmpty()) {
            // 服务端强制校验格式 + 唯一性
            String u = req.getUsername().trim().toLowerCase();
            if (!u.matches("^[a-z][a-z0-9_]{2,49}$")) {
                throw new IllegalArgumentException("账号格式不正确（3-50位字母、数字或下划线，首字母）");
            }
            if (userRepository.existsByUsername(u)) {
                throw new IllegalArgumentException("账号已存在");
            }
            return u;
        }
        // 自动生成
        String base;
        if (req.getEmployeeNo() != null && !req.getEmployeeNo().isEmpty()) {
            base = "emp_" + req.getEmployeeNo();
        } else {
            base = "emp_" + System.currentTimeMillis();
        }
        // 确保不冲突
        String candidate = base.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    /** 初始密码：手机号后 6 位，若无手机号则 123456 */
    private String genInitialPassword(String phone) {
        if (phone != null && phone.length() >= 6) {
            return phone.substring(phone.length() - 6);
        }
        return "123456";
    }
}
