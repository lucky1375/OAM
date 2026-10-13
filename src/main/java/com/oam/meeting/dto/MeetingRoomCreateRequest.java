package com.oam.meeting.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MeetingRoomCreateRequest {

    @NotBlank(message = "请输入会议室名称")
    @Size(max = 50, message = "名称不能超过50字")
    private String name;

    @Size(max = 100, message = "地点不能超过100字")
    private String location;

    private Integer capacity;

    @Size(max = 200, message = "设备不能超过200字")
    private String equipment;

    private Boolean enabled = true;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

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
}