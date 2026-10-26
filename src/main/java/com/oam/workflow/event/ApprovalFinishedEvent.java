package com.oam.workflow.event;

/**
 * 审批流程结束事件（通过 / 驳回 / 撤回），供业务模块回写自身状态。
 */
public class ApprovalFinishedEvent {

    private final Long flowId;
    /** APPROVED / REJECTED / CANCELLED */
    private final String status;
    private final String approverName;
    private final String comment;

    public ApprovalFinishedEvent(Long flowId, String status, String approverName, String comment) {
        this.flowId = flowId;
        this.status = status;
        this.approverName = approverName;
        this.comment = comment;
    }

    public Long getFlowId() { return flowId; }
    public String getStatus() { return status; }
    public String getApproverName() { return approverName; }
    public String getComment() { return comment; }
}
