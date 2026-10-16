package com.oam.announcement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 公告。支持草稿/已发布、置顶，并统计阅读人数。
 */
@Entity
@Table(name = "ann_announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 标题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 正文 */
    @Column(length = 5000)
    private String content;

    /** 发布人用户ID */
    @Column
    private Long publisherUserId;

    /** 发布人姓名 */
    @Column(length = 50)
    private String publisherName;

    /** 状态：DRAFT-草稿，PUBLISHED-已发布 */
    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";

    /** 是否置顶 */
    @Column(nullable = false)
    private Boolean top = false;

    /** 发布时间（未发布为 null） */
    @Column
    private LocalDateTime publishTime;

    /** 已读人数（冗余统计） */
    @Column(nullable = false)
    private Integer readCount = 0;

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
    public Long getPublisherUserId() { return publisherUserId; }
    public void setPublisherUserId(Long publisherUserId) { this.publisherUserId = publisherUserId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getTop() { return top; }
    public void setTop(Boolean top) { this.top = top; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
