package com.oam.meeting.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MeetingCreateRequest {

    @NotBlank(message = "请输入会议主题")
    @Size(max = 100, message = "主题不能超过100字")
    private String title;

    private Long meetingRoomId;

    // 格式 yyyy-MM-dd HH:mm:ss
    private String startTime;
    private String endTime;

    @Size(max = 50, message = "发起人不能超过50字")
    private String organizer;

    @Size(max = 500, message = "参会人不能超过500字")
    private String participants;

    @Size(max = 1000, message = "议程不能超过1000字")
    private String agenda;

    /** SCHEDULED / FINISHED / CANCELLED */
    private String status;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getMeetingRoomId() { return meetingRoomId; }
    public void setMeetingRoomId(Long meetingRoomId) { this.meetingRoomId = meetingRoomId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
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
}