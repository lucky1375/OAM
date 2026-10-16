package com.oam.meeting.dto;

import com.oam.meeting.entity.Meeting;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class MeetingDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String title;
    private Long meetingRoomId;
    private String meetingRoomName;
    private String startTime;
    private String endTime;
    private String organizer;
    private String participants;
    private List<Long> participantIds;
    private String agenda;
    private String status;
    private String remark;
    private String createdAt;

    public static MeetingDto from(Meeting m, String roomName) {
        return from(m, roomName, Collections.emptyList());
    }

    public static MeetingDto from(Meeting m, String roomName, List<Long> participantIds) {
        MeetingDto dto = new MeetingDto();
        dto.id = m.getId();
        dto.title = m.getTitle();
        dto.meetingRoomId = m.getMeetingRoomId();
        dto.meetingRoomName = roomName;
        dto.startTime = m.getStartTime() == null ? null : m.getStartTime().format(FMT);
        dto.endTime = m.getEndTime() == null ? null : m.getEndTime().format(FMT);
        dto.organizer = m.getOrganizer();
        dto.participants = m.getParticipants();
        dto.participantIds = participantIds == null ? Collections.emptyList() : participantIds;
        dto.agenda = m.getAgenda();
        dto.status = m.getStatus();
        dto.remark = m.getRemark();
        dto.createdAt = m.getCreatedAt() == null ? null : m.getCreatedAt().format(FMT);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getMeetingRoomId() { return meetingRoomId; }
    public void setMeetingRoomId(Long meetingRoomId) { this.meetingRoomId = meetingRoomId; }
    public String getMeetingRoomName() { return meetingRoomName; }
    public void setMeetingRoomName(String meetingRoomName) { this.meetingRoomName = meetingRoomName; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
    public List<Long> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
    public String getAgenda() { return agenda; }
    public void setAgenda(String agenda) { this.agenda = agenda; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}