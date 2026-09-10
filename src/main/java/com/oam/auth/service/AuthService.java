package com.oam.auth.service;

import com.oam.auth.dto.LoginRequest;
import com.oam.common.security.JwtUtil;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * @return 登录成功后的 token，失败返回 null
     */
    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
        if (user == null || !user.getEnabled()
                || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return null;
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }
}
