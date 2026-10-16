package com.oam.meeting.dto;

import com.oam.user.entity.User;

/** 账号下拉选项（仅暴露必要字段，供普通用户选择参会人） */
public class AccountOptionDto {

    private Long id;
    private String username;
    private String nickname;
    private String displayName;

    public static AccountOptionDto from(User u) {
        AccountOptionDto dto = new AccountOptionDto();
        dto.id = u.getId();
        dto.username = u.getUsername();
        dto.nickname = u.getNickname();
        dto.displayName = u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
