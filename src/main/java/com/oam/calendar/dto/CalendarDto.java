package com.oam.calendar.dto;

import com.oam.calendar.entity.CalendarEvent;

import java.time.format.DateTimeFormatter;

public class CalendarDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String title;
    private String startTime;
    private String endTime;
    private Boolean allDay;
    private String location;
    private Integer remindMinutes;
    private String remark;
    private Long ownerUserId;
    private String ownerName;
    private String createdAt;

    public static CalendarDto from(CalendarEvent e, String ownerName) {
        CalendarDto dto = new CalendarDto();
        dto.id = e.getId();
        dto.title = e.getTitle();
        dto.startTime = e.getStartTime() == null ? null : e.getStartTime().format(FMT);
        dto.endTime = e.getEndTime() == null ? null : e.getEndTime().format(FMT);
        dto.allDay = e.getAllDay();
        dto.location = e.getLocation();
        dto.remindMinutes = e.getRemindMinutes();
        dto.remark = e.getRemark();
        dto.ownerUserId = e.getOwnerUserId();
        dto.ownerName = ownerName;
        dto.createdAt = e.getCreatedAt() == null ? null : e.getCreatedAt().format(FMT);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
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
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}