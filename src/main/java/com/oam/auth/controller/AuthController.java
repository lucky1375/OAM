package com.oam.auth.controller;

import com.oam.auth.dto.LoginRequest;
import com.oam.auth.service.AuthService;
import com.oam.common.dto.ApiResponse;
import com.oam.user.dto.UserDto;
import com.oam.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        if (token == null) {
            return ApiResponse.error("账号或密码错误，或账号已被停用");
        }
        UserDto userDto = userService.getDtoByUsername(loginRequest.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userDto);
        return ApiResponse.ok(data);
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ApiResponse.ok(userService.getDtoByUsername(username));
    }
}
