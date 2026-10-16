package com.oam.common.config;

import com.oam.forum.entity.ForumSection;
import com.oam.forum.repository.ForumSectionRepository;
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
    private final ForumSectionRepository forumSectionRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleService roleService,
                           ForumSectionRepository forumSectionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.forumSectionRepository = forumSectionRepository;
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
        initForumSections();
    }

    /** 空库时预置企业论坛默认版块，避免无版块时无法发帖 */
    private void initForumSections() {
        if (forumSectionRepository.count() > 0) {
            return;
        }
        String[][] defaults = {
                {"通知公告", "公司通知与公告发布", "0"},
                {"技术交流", "技术分享与问题讨论", "1"},
                {"自由闲聊", "工作之外，畅所欲言", "2"}
        };
        for (String[] d : defaults) {
            ForumSection s = new ForumSection();
            s.setName(d[0]);
            s.setDescription(d[1]);
            s.setSort(Integer.parseInt(d[2]));
            s.setCreatedBy("system");
            forumSectionRepository.save(s);
        }
        log.info("已预置企业论坛默认版块 {} 个", defaults.length);
    }
}
