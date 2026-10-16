package com.oam.forum.dto;

import com.oam.forum.entity.ForumComment;

import java.time.format.DateTimeFormatter;

public class CommentDto {

    private Long id;
    private String authorName;
    private String content;
    private String createdAt;

    public static CommentDto from(ForumComment c) {
        CommentDto dto = new CommentDto();
        dto.id = c.getId();
        dto.authorName = c.getAuthorName();
        dto.content = c.getContent();
        dto.createdAt = c.getCreatedAt() == null ? null
                : c.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
