package com.oam.user.service;

import com.oam.common.permission.PermissionCatalog;
import com.oam.common.security.SecurityUtil;
import com.oam.user.dto.UserCreateRequest;
import com.oam.user.dto.UserDto;
import com.oam.user.dto.UserUpdateRequest;
import com.oam.user.entity.Role;
import com.oam.user.entity.RolePermission;
import com.oam.user.entity.User;
import com.oam.user.entity.UserPermission;
import com.oam.user.repository.RolePermissionRepository;
import com.oam.user.repository.UserPermissionRepository;
import com.oam.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserPermissionRepository userPermissionRepository,
                       RolePermissionRepository rolePermissionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> list() {
        return list(null);
    }

    public List<UserDto> list(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        return userRepository.findAllByOrderByCreatedAtAsc().stream()
                .filter(u -> kw.isEmpty()
                        || (u.getUsername() != null && u.getUsername().contains(kw))
                        || (u.getNickname() != null && u.getNickname().contains(kw)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto get(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        return toDto(user);
    }

    @Transactional
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("账号已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCreatedBy(SecurityUtil.currentUsername());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        user.setNickname(request.getNickname());
        user.setRole(request.getRole());
        user.setEnabled(request.getEnabled());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUpdatedBy(SecurityUtil.currentUsername());
        user.setUpdatedAt(LocalDateTime.now());
        return toDto(userRepository.save(user));
    }

    /**
     * 管理员重置指定用户密码。
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(SecurityUtil.currentUsername());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        userRepository.delete(user);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        dto.setEnabled(user.getEnabled());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt() == null ? null : user.getCreatedAt().format(FORMATTER));
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());
        dto.setUpdatedAt(user.getUpdatedAt() == null ? null : user.getUpdatedAt().format(FORMATTER));
        dto.setPermissions(effectivePermissions(user));
        return dto;
    }

    public UserDto getDtoByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        return toDto(user);
    }

    /**
     * 返回用户当前被授权的权限 = 角色默认权限 ∪ 用户显式授权。
     * ADMIN 角色默认拥有全部权限（不走 RolePermission 表）。
     */
    public List<String> effectivePermissions(User user) {
        Set<String> merged = new LinkedHashSet<>();
        // 1) 角色默认权限
        if (user.getRole() == Role.ADMIN) {
            merged.addAll(PermissionCatalog.allCodes());
        } else {
            merged.addAll(rolePermissionRepository.findByRole(user.getRole()).stream()
                    .map(RolePermission::getPermissionCode)
                    .collect(Collectors.toList()));
        }
        // 2) 用户显式授权（覆盖/扩展）
        merged.addAll(userPermissionRepository.findByUserId(user.getId()).stream()
                .map(UserPermission::getPermissionCode)
                .collect(Collectors.toList()));
        return new ArrayList<>(merged);
    }

    /**
     * 查询某用户被授权的权限（权限管理界面编辑用）。
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAssignedPermissions(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        List<String> codes;
        if (user.getRole() == Role.ADMIN) {
            codes = PermissionCatalog.allCodes();
        } else {
            Set<String> merged = new LinkedHashSet<>();
            merged.addAll(rolePermissionRepository.findByRole(user.getRole()).stream()
                    .map(RolePermission::getPermissionCode).collect(Collectors.toList()));
            merged.addAll(userPermissionRepository.findByUserId(userId).stream()
                    .map(UserPermission::getPermissionCode).collect(Collectors.toList()));
            codes = new ArrayList<>(merged);
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("role", user.getRole());
        result.put("permissions", codes);
        return result;
    }

    /**
     * 给用户重新分配权限。
     */
    @Transactional
    public void updatePermissions(Long userId, List<String> permissions) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("用户不存在"));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("管理员默认拥有全部权限，无需分配");
        }
        Set<String> toAssign = new HashSet<>(permissions == null ? new ArrayList<>() : permissions);
        for (String code : toAssign) {
            if (!PermissionCatalog.contains(code)) {
                throw new IllegalArgumentException("包含无效权限码：" + code);
            }
        }
        userPermissionRepository.deleteByUserId(userId);
        for (String code : toAssign) {
            UserPermission permission = new UserPermission();
            permission.setUserId(userId);
            permission.setPermissionCode(code);
            userPermissionRepository.save(permission);
        }
    }
}
