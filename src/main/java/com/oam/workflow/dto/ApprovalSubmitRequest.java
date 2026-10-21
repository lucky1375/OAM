package com.oam.workflow.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ApprovalSubmitRequest {

    @NotBlank(message = "请选择流程类型")
    private String type;

    @NotBlank(message = "请输入标题")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @NotBlank(message = "请填写申请内容")
    @Size(max = 2000, message = "内容不能超过2000字")
    private String content;

    /** 审批人；流程类型已配置固定审批节点时可不传 */
    private Long approverUserId;

    /** 请假开始时间，格式 yyyy-MM-dd HH:mm:ss（仅请假流程必填） */
    private String startTime;

    /** 请假结束时间，格式 yyyy-MM-dd HH:mm:ss（仅请假流程必填） */
    private String endTime;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
