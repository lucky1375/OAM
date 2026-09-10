package com.oam.user.dto;

import java.util.List;

public class UserPermissionUpdateRequest {

    /** 要赋予的权限码集合 */
    private List<String> permissions;

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
