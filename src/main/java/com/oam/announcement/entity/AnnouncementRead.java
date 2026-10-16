package com.oam.announcement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 公告阅读记录（公告 + 用户 唯一），用于阅读统计。
 */
@Entity
@Table(name = "ann_read", uniqueConstraints = @UniqueConstraint(columnNames = {"announcementId", "userId"}))
public class AnnouncementRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 公告ID */
    @Column(nullable = false)
    private Long announcementId;

    /** 阅读用户ID */
    @Column(nullable = false)
    private Long userId;

    /** 阅读用户姓名（冗余，便于统计展示） */
    @Column(length = 50)
    private String userName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
