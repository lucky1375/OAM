package com.oam.workflow.config;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Activiti 集成配置。
 * <p>Activiti 7 的 ProcessEngineAutoConfiguration 需要 UserGroupManager，
 * 官方实现依赖 Spring Security 的 UserDetailsService；本项目使用 JWT 认证，
 * 因此在这里提供一个空实现，审批人通过 BPMN 的 assignee 变量直接指定。</p>
 */
@Configuration
public class ActivitiConfig {

    @Bean
    public UserGroupManager userGroupManager() {
        return new UserGroupManager() {
            @Override
            public List<String> getUserGroups(String userId) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getUserRoles(String userId) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getGroups() {
                return Collections.emptyList();
            }

            @Override
            public List<String> getUsers() {
                return Collections.emptyList();
            }
        };
    }
}
