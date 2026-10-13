package com.oam.common.config;

import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import com.oam.user.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统初始化：
 * 1) 空库时创建默认管理员账号 admin / admin123
 * 2) 初始化 ADMIN / USER 两个角色的默认权限
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setNickname("系统管理员");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("已创建默认管理员账号：admin / admin123");
        }
        roleService.seedDefaults();
    }
}
