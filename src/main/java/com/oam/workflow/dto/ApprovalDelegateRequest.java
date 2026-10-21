package com.oam.workflow.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ApprovalDelegateRequest {

    @NotNull(message = "请选择转办人")
    private Long targetUserId;

    @Size(max = 500, message = "转办说明不能超过500字")
    private String comment;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
