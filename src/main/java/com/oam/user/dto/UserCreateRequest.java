package com.oam.user.dto;

import com.oam.user.entity.Role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserCreateRequest {

    @NotBlank(message = "请输入账号")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "账号需为3-50位字母、数字或下划线")
    private String username;

    @NotBlank(message = "请输入昵称")
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @NotBlank(message = "请输入初始密码")
    @Size(min = 6, max = 50, message = "密码长度需为6-50位")
    private String password;

    @NotNull(message = "请选择角色")
    private Role role;

    private Boolean enabled = true;

    @Size(max = 50, message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不正确")
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
