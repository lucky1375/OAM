package com.oam.announcement.dto;

import com.oam.announcement.entity.AnnouncementRead;

import java.time.format.DateTimeFormatter;

/** 公告阅读记录（阅读统计条目） */
public class ReadStatDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long userId;
    private String userName;
    private String readTime;

    public static ReadStatDto from(AnnouncementRead r) {
        ReadStatDto dto = new ReadStatDto();
        dto.userId = r.getUserId();
        dto.userName = r.getUserName();
        dto.readTime = r.getCreatedAt() == null ? null : r.getCreatedAt().format(FMT);
        return dto;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getReadTime() { return readTime; }
    public void setReadTime(String readTime) { this.readTime = readTime; }
}
