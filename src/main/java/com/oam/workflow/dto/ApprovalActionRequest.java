package com.oam.workflow.dto;

import javax.validation.constraints.Size;

public class ApprovalActionRequest {

    @Size(max = 500, message = "审批意见不能超过500字")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
