package com.oam.meeting.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户账号分组（个人分组：ownerUserId 为创建者）。
 * 用于在预约会议时把一组固定账号整组选入参会人。
 */
@Entity
@Table(name = "mtg_user_group")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分组归属用户ID（仅本人可见/可管理） */
    @Column(nullable = false)
    private Long ownerUserId;

    /** 分组名称 */
    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdBy;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
