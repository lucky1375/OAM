package com.oam.expense.dto;

/** 提交报销单审批 */
public class ExpenseSubmitRequest {

    /** 审批人 ID：报销类型未配置固定审批节点时必填 */
    private Long approverUserId;

    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
}
