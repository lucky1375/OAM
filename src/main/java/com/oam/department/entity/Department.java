package com.oam.department.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 部门（支持多层级树形结构，无固定层级上限）。
 * 同级（相同 parentId 下）名称唯一；根节点 parentId 为 null。
 */
@Entity
@Table(name = "sys_department",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parentId", "name"}))
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 部门名称 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 上级部门ID，根节点为 null */
    @Column
    private Long parentId;

    /** 层级：0=一级 */
    @Column(nullable = false)
    private Integer level;

    /** 排序号 */
    @Column(nullable = false)
    private Integer sort = 0;

    /** 负责人 */
    @Column(length = 50)
    private String leaderName;

    /** 联系电话 */
    @Column(length = 20)
    private String phone;

    /** 状态 */
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
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
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
