package com.oam.suggestion.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 建议反馈：员工提交意见/建议，管理员回复并跟踪处理状态。
 */
@Entity
@Table(name = "sgt_suggestion")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 标题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 内容 */
    @Column(nullable = false, length = 5000)
    private String content;

    /** 分类：IMPROVEMENT-改进建议，FEATURE-功能需求，BUG-问题反馈，OTHER-其他 */
    @Column(nullable = false, length = 20)
    private String category = "IMPROVEMENT";

    /** 状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，CLOSED-已关闭 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** 提交人用户ID */
    @Column(nullable = false)
    private Long submitterUserId;

    /** 提交人姓名 */
    @Column(length = 50)
    private String submitterName;

    /** 是否匿名提交 */
    @Column(nullable = false)
    private Boolean anonymous = false;

    /** 管理员回复内容 */
    @Column(length = 2000)
    private String replyContent;

    /** 回复人姓名 */
    @Column(length = 50)
    private String replierName;

    /** 回复时间 */
    @Column
    private LocalDateTime repliedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getSubmitterUserId() { return submitterUserId; }
    public void setSubmitterUserId(Long submitterUserId) { this.submitterUserId = submitterUserId; }
    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }
    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean anonymous) { this.anonymous = anonymous; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
    public String getReplierName() { return replierName; }
    public void setReplierName(String replierName) { this.replierName = replierName; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
