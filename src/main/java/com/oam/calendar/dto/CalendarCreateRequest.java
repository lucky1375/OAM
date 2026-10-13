package com.oam.calendar.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CalendarCreateRequest {

    @NotBlank(message = "请输入日程标题")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    // 格式 yyyy-MM-dd HH:mm:ss
    private String startTime;
    private String endTime;
    private Boolean allDay = false;

    @Size(max = 100, message = "地点不能超过100字")
    private String location;

    private Integer remindMinutes;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    /** 归属人 User.id；留空默认当前登录用户 */
    private Long ownerUserId;

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
}