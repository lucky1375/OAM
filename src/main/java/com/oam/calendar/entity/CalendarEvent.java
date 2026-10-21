package com.oam.calendar.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 日程安排（个人/团队日历）。
 */
@Entity
@Table(name = "scd_calendar")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 日程标题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 开始时间 */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** 结束时间 */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** 是否全天 */
    @Column(nullable = false)
    private Boolean allDay = false;

    /** 地点 */
    @Column(length = 100)
    private String location;

    /** 提前提醒（分钟），null 表示不提醒 */
    @Column
    private Integer remindMinutes;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /** 归属人（User.id） */
    @Column
    private Long ownerUserId;

    /** 来源会议（mtg_meeting.id），会议预定自动同步生成的日程用该字段关联 */
    @Column
    private Long sourceMeetingId;

    /** 来源审批流程（wf_flow.id），请假审批通过后自动生成的日程用该字段关联 */
    @Column
    private Long sourceFlowId;

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
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Boolean getAllDay() { return allDay; }
    public void setAllDay(Boolean allDay) { this.allDay = allDay; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getRemindMinutes() { return remindMinutes; }
    public void setRemindMinutes(Integer remindMinutes) { this.remindMinutes = remindMinutes; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public Long getSourceMeetingId() { return sourceMeetingId; }
    public void setSourceMeetingId(Long sourceMeetingId) { this.sourceMeetingId = sourceMeetingId; }
    public Long getSourceFlowId() { return sourceFlowId; }
    public void setSourceFlowId(Long sourceFlowId) { this.sourceFlowId = sourceFlowId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}