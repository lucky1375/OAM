package com.oam.department.dto;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门信息 DTO，含 children 字段可直接供前端 el-tree / el-table 树表使用。
 */
public class DepartmentDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private String leaderName;
    private String phone;
    private Boolean enabled;
    private String remark;
    private String createdAt;
    private String createdBy;
    private String updatedBy;
    private String updatedAt;
    /** 完整路径名称，如 "集团 / 研发中心 / 后端组" */
    private String pathNames;
    /** 子部门（树形） */
    private List<DepartmentDto> children = new ArrayList<>();

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
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getPathNames() { return pathNames; }
    public void setPathNames(String pathNames) { this.pathNames = pathNames; }
    public List<DepartmentDto> getChildren() { return children; }
    public void setChildren(List<DepartmentDto> children) { this.children = children; }

    public static String fmt(java.time.LocalDateTime t) {
        return t == null ? null : t.format(FMT);
    }
}
