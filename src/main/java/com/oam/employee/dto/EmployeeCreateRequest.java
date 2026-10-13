package com.oam.employee.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmployeeCreateRequest {

    @NotBlank(message = "请输入姓名")
    @Size(max = 50, message = "姓名不能超过50字")
    private String name;

    @Size(max = 30, message = "工号不能超过30字")
    private String employeeNo;

    @Size(max = 2, message = "性别长度不正确")
    private String gender;

    @Size(max = 20, message = "手机号长度不正确")
    private String phone;

    @Size(max = 100, message = "邮箱不能超过100字")
    private String email;

    private Long departmentId;
    private Long positionId;
    private Boolean enabled = true;

    /** 手动指定账号；留空则自动生成（规则：姓名拼音+工号后缀） */
    private String username;

    /** 初始密码；留空则自动生成（手机号后6位或 123456） */
    private String initialPassword;

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
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }
}
