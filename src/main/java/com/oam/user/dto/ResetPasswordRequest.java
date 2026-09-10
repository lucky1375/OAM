package com.oam.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 50, message = "密码长度需为6-50位")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
