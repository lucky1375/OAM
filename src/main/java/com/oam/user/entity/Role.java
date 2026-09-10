package com.oam.user.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色：ADMIN 可管理全部账号，USER 为普通用户。
 */
public enum Role {
    ADMIN,
    USER;

    /**
     * 角色对应的默认菜单权限码（预留）。
     * 实际的菜单与按钮权限来自用户授权（sys_user_permission 表），
     * 管理员通过 {@code effectivePermissions} 默认拥有全部权限。
     */
    public List<String> permissions() {
        List<String> list = new ArrayList<>();
        if (this == ADMIN) {
            list.add("user");
        }
        return list;
    }
}
