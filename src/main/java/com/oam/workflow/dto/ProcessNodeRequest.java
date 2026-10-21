package com.oam.workflow.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProcessNodeRequest {

    @NotBlank(message = "请输入节点名称")
    @Size(max = 50, message = "节点名称不能超过50字")
    private String nodeName;

    @NotNull(message = "请选择审批人")
    private Long approverUserId;

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
}
