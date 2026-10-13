package com.oam.employee.dto;

import com.oam.employee.entity.Employee;
import com.oam.user.entity.User;

import java.time.format.DateTimeFormatter;

public class EmployeeDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String name;
    private String employeeNo;
    private String gender;
    private String phone;
    private String email;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionName;
    private Long userId;
    private String username;
    private String userRole;
    private Boolean userEnabled;
    private Boolean enabled;
    private String createdAt;
    private String createdBy;
    private String updatedAt;

    public static EmployeeDto from(Employee e, String deptName, String posName, User user) {
        EmployeeDto dto = new EmployeeDto();
        dto.id = e.getId();
        dto.name = e.getName();
        dto.employeeNo = e.getEmployeeNo();
        dto.gender = e.getGender();
        dto.phone = e.getPhone();
        dto.email = e.getEmail();
        dto.departmentId = e.getDepartmentId();
        dto.departmentName = deptName;
        dto.positionId = e.getPositionId();
        dto.positionName = posName;
        dto.userId = e.getUserId();
        dto.username = user == null ? null : user.getUsername();
        dto.userRole = user == null ? null : user.getRole().name();
        dto.userEnabled = user == null ? null : user.getEnabled();
        dto.enabled = e.getEnabled();
        dto.createdAt = e.getCreatedAt() == null ? null : e.getCreatedAt().format(FMT);
        dto.createdBy = e.getCreatedBy();
        dto.updatedAt = e.getUpdatedAt() == null ? null : e.getUpdatedAt().format(FMT);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public Boolean getUserEnabled() { return userEnabled; }
    public void setUserEnabled(Boolean userEnabled) { this.userEnabled = userEnabled; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
