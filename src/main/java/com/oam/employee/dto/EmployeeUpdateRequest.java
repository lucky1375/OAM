package com.oam.employee.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmployeeUpdateRequest {
    @NotBlank(message = "请输入姓名")
    @Size(max = 50)
    private String name;

    @Size(max = 30)
    private String employeeNo;

    @Size(max = 2)
    private String gender;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String email;

    private Long departmentId;
    private Long positionId;
    private Boolean enabled = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
