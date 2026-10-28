package com.oam.expense.dto;

import java.math.BigDecimal;

/** 报销统计（当前用户维度） */
public class ExpenseStatsDto {

    private long draft;
    private long pending;
    private long approved;
    private long rejected;
    private long cancelled;
    /** 待我审批的报销单数量 */
    private long todo;
    /** 已通过报销累计金额 */
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    public long getDraft() { return draft; }
    public void setDraft(long draft) { this.draft = draft; }
    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }
    public long getApproved() { return approved; }
    public void setApproved(long approved) { this.approved = approved; }
    public long getRejected() { return rejected; }
    public void setRejected(long rejected) { this.rejected = rejected; }
    public long getCancelled() { return cancelled; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }
    public long getTodo() { return todo; }
    public void setTodo(long todo) { this.todo = todo; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
}
