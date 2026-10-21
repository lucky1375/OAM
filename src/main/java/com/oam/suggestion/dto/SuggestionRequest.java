package com.oam.suggestion.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SuggestionRequest {

    @NotBlank(message = "请输入标题")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @NotBlank(message = "请输入内容")
    @Size(max = 5000, message = "内容不能超过5000字")
    private String content;

    /** IMPROVEMENT / FEATURE / BUG / OTHER */
    private String category;

    /** 是否匿名提交 */
    private Boolean anonymous;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean anonymous) { this.anonymous = anonymous; }
}
