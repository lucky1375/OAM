package com.oam.permission.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.common.permission.PermissionCatalog;
import com.oam.user.dto.UserPermissionUpdateRequest;
import com.oam.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final UserService userService;

    public PermissionController(UserService userService) {
        this.userService = userService;
    }

    /** 功能树（模块 + 操作按钮） */
    @GetMapping("/tree")
    public ApiResponse<List<PermissionCatalog.Module>> tree() {
        return ApiResponse.ok(PermissionCatalog.tree());
    }

    /** 查询某用户已授权的权限 */
    @GetMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> assigned(@PathVariable Long id) {
        return ApiResponse.ok(userService.getAssignedPermissions(id));
    }

    /** 给用户重新分配权限 */
    @PutMapping("/users/{id}")
    public ApiResponse<Void> assign(@PathVariable Long id,
                                    @Valid @RequestBody UserPermissionUpdateRequest request) {
        userService.updatePermissions(id, request.getPermissions());
        return ApiResponse.ok("授权成功", null);
    }
}
