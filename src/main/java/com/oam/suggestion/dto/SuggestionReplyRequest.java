package com.oam.suggestion.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SuggestionReplyRequest {

    @NotBlank(message = "请输入回复内容")
    @Size(max = 2000, message = "回复不能超过2000字")
    private String replyContent;

    /** 回复后状态：PROCESSING / RESOLVED / CLOSED，默认 RESOLVED */
    private String status;

    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
