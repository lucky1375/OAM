package com.oam.department.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class DepartmentCreateRequest {

    @NotBlank(message = "请输入部门名称")
    @Size(max = 50, message = "部门名称不能超过50字")
    private String name;

    /** 上级部门ID，根节点传 null */
    private Long parentId;

    private Integer sort = 0;

    @Size(max = 50, message = "负责人名称不能超过50字")
    private String leaderName;

    @Size(max = 20, message = "手机号长度不正确")
    private String phone;

    private Boolean enabled = true;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
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
}
