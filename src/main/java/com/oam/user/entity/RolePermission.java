package com.oam.user.entity;

import javax.persistence.*;

/**
 * 角色默认权限（按角色粒度配置的权限码集合）。
 * 实际生效权限 = 角色默认权限 ∪ 用户级覆盖权限。
 */
@Entity
@Table(name = "sys_role_permission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role", "permissionCode"}))
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 50)
    private String permissionCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
}
