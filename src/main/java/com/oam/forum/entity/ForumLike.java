package com.oam.forum.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 论坛点赞（帖子 + 用户 唯一）。
 */
@Entity
@Table(name = "frm_like", uniqueConstraints = @UniqueConstraint(columnNames = {"postId", "userId"}))
public class ForumLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 帖子ID */
    @Column(nullable = false)
    private Long postId;

    /** 点赞用户ID */
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
