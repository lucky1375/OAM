package com.oam.meeting.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会议。
 */
@Entity
@Table(name = "mtg_meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会议主题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 会议室 */
    @Column
    private Long meetingRoomId;

    /** 开始时间 */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** 结束时间 */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** 发起人 */
    @Column(length = 50)
    private String organizer;

    /** 参会人（逗号分隔） */
    @Column(length = 500)
    private String participants;

    /** 会议议程/内容 */
    @Column(length = 1000)
    private String agenda;

    /** 状态：SCHEDULED 待开 / FINISHED 已结束 / CANCELLED 已取消 */
    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    /** 备注 */
    @Column(length = 500)
    private String remark;

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
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getMeetingRoomId() { return meetingRoomId; }
    public void setMeetingRoomId(Long meetingRoomId) { this.meetingRoomId = meetingRoomId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
    public String getAgenda() { return agenda; }
    public void setAgenda(String agenda) { this.agenda = agenda; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}