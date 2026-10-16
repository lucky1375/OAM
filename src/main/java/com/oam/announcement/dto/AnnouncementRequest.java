package com.oam.announcement.dto;

import javax.validation.constraints.NotBlank;

public class AnnouncementRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    /** 状态：DRAFT-草稿，PUBLISHED-已发布（默认发布） */
    private String status;

    /** 是否置顶 */
    private Boolean top;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getTop() { return top; }
    public void setTop(Boolean top) { this.top = top; }
}
