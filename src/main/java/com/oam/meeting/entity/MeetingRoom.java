package com.oam.meeting.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会议室。
 */
@Entity
@Table(name = "mtg_room")
public class MeetingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会议室名称 */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** 地点 */
    @Column(length = 100)
    private String location;

    /** 容纳人数 */
    @Column
    private Integer capacity;

    /** 设备（如投影、白板、视频会议） */
    @Column(length = 200)
    private String equipment;

    /** 是否可用 */
    @Column(nullable = false)
    private Boolean enabled = true;

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}