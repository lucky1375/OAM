package com.oam.meeting.entity;

import javax.persistence.*;

/**
 * 用户账号分组明细（分组 + 用户 唯一）。
 */
@Entity
@Table(name = "mtg_user_group_member", uniqueConstraints = @UniqueConstraint(columnNames = {"groupId", "userId"}))
public class UserGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分组ID */
    @Column(nullable = false)
    private Long groupId;

    /** 成员用户ID */
    @Column(nullable = false)
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
