package com.oam.forum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PostCreateRequest {

    @NotNull(message = "请选择版块")
    private Long sectionId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @Size(max = 5000, message = "内容不能超过5000字")
    private String content;

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
