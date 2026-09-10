package com.oam.user.entity;

import javax.persistence.*;

@Entity
@Table(name = "sys_user_permission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "permissionCode"}))
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    /** 权限码，如 user:view */
    @Column(nullable = false, length = 50)
    private String permissionCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
}
