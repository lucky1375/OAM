package com.oam.user.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.user.entity.Role;
import com.oam.user.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 按角色配置权限（ADMIN / USER）。
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Map<String, Object>> roles = Arrays.stream(Role.values()).map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", r.name());
            m.put("label", r == Role.ADMIN ? "管理员" : "普通用户");
            m.put("admin", r == Role.ADMIN);
            m.put("permissions", roleService.listByRole(r));
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.ok(roles);
    }

    @PutMapping("/{role}/permissions")
    public ApiResponse<Void> configure(@PathVariable String role,
                                       @RequestBody List<String> codes) {
        Role r;
        try {
            r = Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("角色不存在");
        }
        roleService.configureRole(r, codes);
        return ApiResponse.ok("角色权限已更新", null);
    }
}
