package com.oam.position.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class PositionCreateRequest {
    @NotBlank(message = "请输入岗位名称")
    @Size(max = 50, message = "岗位名称不能超过50字")
    private String name;

    @Size(max = 200, message = "岗位描述不能超过200字")
    private String description;

    /** 归属部门ID */
    private Long departmentId;

    private Boolean enabled = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
