package com.oam.forum.dto;

import com.oam.forum.entity.ForumPost;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostDetailDto {

    private Long id;
    private Long sectionId;
    private String sectionName;
    private String title;
    private String content;
    private String authorName;
    private Integer likeCount;
    private Integer commentCount;
    private String createdAt;
    private boolean likedByMe;
    private List<CommentDto> comments;

    public static PostDetailDto from(ForumPost p, String sectionName, boolean likedByMe,
                                     List<CommentDto> comments) {
        PostDetailDto dto = new PostDetailDto();
        dto.id = p.getId();
        dto.sectionId = p.getSectionId();
        dto.sectionName = sectionName;
        dto.title = p.getTitle();
        dto.content = p.getContent();
        dto.authorName = p.getAuthorName();
        dto.likeCount = p.getLikeCount();
        dto.commentCount = p.getCommentCount();
        dto.createdAt = p.getCreatedAt() == null ? null
                : p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dto.likedByMe = likedByMe;
        dto.comments = comments;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
    public List<CommentDto> getComments() { return comments; }
    public void setComments(List<CommentDto> comments) { this.comments = comments; }
}
