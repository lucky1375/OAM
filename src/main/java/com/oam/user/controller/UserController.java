package com.oam.user.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.common.util.ExcelUtil;
import com.oam.user.dto.*;
import com.oam.user.service.UserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserDto>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userService.list(keyword));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        List<String[]> rows = new ArrayList<>();
        for (UserDto u : userService.list()) {
            rows.add(new String[]{
                    String.valueOf(u.getId()),
                    notNull(u.getUsername()),
                    notNull(u.getNickname()),
                    u.getRole() != null && u.getRole().name().equals("ADMIN") ? "管理员" : "普通用户",
                    u.getEnabled() != null && u.getEnabled() ? "启用" : "停用",
                    notNull(u.getEmail()),
                    notNull(u.getPhone()),
                    notNull(u.getCreatedAt())
            });
        }
        byte[] bytes = ExcelUtil.toXlsxBytes("账号",
                new String[]{"ID", "登录账号", "昵称", "角色", "状态", "邮箱", "手机号", "创建时间"},
                rows);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType
                .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("users.xlsx", StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(disposition);
        return new ResponseEntity<>(bytes, headers, 200);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.get(id));
    }

    @PostMapping
    public ApiResponse<UserDto> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok("创建成功", userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok("更新成功", userService.update(id, request));
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return ApiResponse.ok("密码重置成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    private String notNull(String value) {
        return value == null ? "" : value;
    }
}
