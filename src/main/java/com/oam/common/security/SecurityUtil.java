package com.oam.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户信息的工具。
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /** 当前登录用户名，未登录返回 null */
    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        if (principal instanceof String) {
            // 匿名用户等场景返回 anonymousUser
            return "anonymousUser".equals(principal) ? null : (String) principal;
        }
        return null;
    }
}
