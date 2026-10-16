package com.oam.meeting.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会议参会人（会议 + 用户 唯一），由账号选择产生。
 */
@Entity
@Table(name = "mtg_participant", uniqueConstraints = @UniqueConstraint(columnNames = {"meetingId", "userId"}))
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会议ID */
    @Column(nullable = false)
    private Long meetingId;

    /** 参会用户ID */
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
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
