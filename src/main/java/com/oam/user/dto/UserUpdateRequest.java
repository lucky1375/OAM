package com.oam.user.dto;

import com.oam.user.entity.Role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserUpdateRequest {

    @NotBlank(message = "请输入昵称")
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @NotNull(message = "请选择角色")
    private Role role;

    @NotNull(message = "请选择启用状态")
    private Boolean enabled;

    @Size(max = 50, message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不正确")
    private String phone;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
