package com.oam.suggestion.dto;

import com.oam.suggestion.entity.Suggestion;

import java.time.format.DateTimeFormatter;

public class SuggestionDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String title;
    private String content;
    private String category;
    private String status;
    private String submitterName;
    private Boolean anonymous;
    private Boolean mine;
    private String replyContent;
    private String replierName;
    private String repliedAt;
    private String createdAt;
    private String updatedAt;

    public static SuggestionDto from(Suggestion s, Long currentUserId) {
        SuggestionDto dto = new SuggestionDto();
        dto.id = s.getId();
        dto.title = s.getTitle();
        dto.content = s.getContent();
        dto.category = s.getCategory();
        dto.status = s.getStatus();
        dto.anonymous = s.getAnonymous();
        dto.mine = currentUserId != null && currentUserId.equals(s.getSubmitterUserId());
        // 匿名提交对非本人隐藏姓名
        if (Boolean.TRUE.equals(s.getAnonymous()) && !Boolean.TRUE.equals(dto.mine)) {
            dto.submitterName = "匿名用户";
        } else {
            dto.submitterName = s.getSubmitterName();
        }
        dto.replyContent = s.getReplyContent();
        dto.replierName = s.getReplierName();
        dto.repliedAt = s.getRepliedAt() == null ? null : s.getRepliedAt().format(FMT);
        dto.createdAt = s.getCreatedAt() == null ? null : s.getCreatedAt().format(FMT);
        dto.updatedAt = s.getUpdatedAt() == null ? null : s.getUpdatedAt().format(FMT);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }
    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean anonymous) { this.anonymous = anonymous; }
    public Boolean getMine() { return mine; }
    public void setMine(Boolean mine) { this.mine = mine; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
    public String getReplierName() { return replierName; }
    public void setReplierName(String replierName) { this.replierName = replierName; }
    public String getRepliedAt() { return repliedAt; }
    public void setRepliedAt(String repliedAt) { this.repliedAt = repliedAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
