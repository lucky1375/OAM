package com.oam.meeting.dto;

import com.oam.meeting.entity.MeetingRoom;

import java.time.format.DateTimeFormatter;

public class MeetingRoomDto {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String equipment;
    private Boolean enabled;
    private String remark;
    private String createdAt;

    public static MeetingRoomDto from(MeetingRoom r) {
        MeetingRoomDto dto = new MeetingRoomDto();
        dto.id = r.getId();
        dto.name = r.getName();
        dto.location = r.getLocation();
        dto.capacity = r.getCapacity();
        dto.equipment = r.getEquipment();
        dto.enabled = r.getEnabled();
        dto.remark = r.getRemark();
        dto.createdAt = r.getCreatedAt() == null ? null
                : r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}