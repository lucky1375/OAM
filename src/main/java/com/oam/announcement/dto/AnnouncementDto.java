package com.oam.announcement.dto;

import com.oam.announcement.entity.Announcement;

import java.time.format.DateTimeFormatter;

public class AnnouncementDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String title;
    private String summary;
    private String publisherName;
    private String status;
    private Boolean top;
    private String publishTime;
    private String createdAt;
    private Integer readCount;
    private Boolean readByMe;

    public static AnnouncementDto from(Announcement a, boolean readByMe) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.id = a.getId();
        dto.title = a.getTitle();
        dto.summary = summarize(a.getContent());
        dto.publisherName = a.getPublisherName();
        dto.status = a.getStatus();
        dto.top = a.getTop();
        dto.publishTime = a.getPublishTime() == null ? null : a.getPublishTime().format(FMT);
        dto.createdAt = a.getCreatedAt() == null ? null : a.getCreatedAt().format(FMT);
        dto.readCount = a.getReadCount();
        dto.readByMe = readByMe;
        return dto;
    }

    private static String summarize(String content) {
        if (content == null) return "";
        String c = content.trim().replaceAll("\\s+", " ");
        return c.length() > 80 ? c.substring(0, 80) + "…" : c;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getTop() { return top; }
    public void setTop(Boolean top) { this.top = top; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public Boolean getReadByMe() { return readByMe; }
    public void setReadByMe(Boolean readByMe) { this.readByMe = readByMe; }
}
