package com.oam.workflow.dto;

/** 流程审批统计（当前用户视角） */
public class ApprovalStatsDto {

    private long myPending;
    private long todo;
    private long approved;
    private long rejected;

    public long getMyPending() { return myPending; }
    public void setMyPending(long myPending) { this.myPending = myPending; }
    public long getTodo() { return todo; }
    public void setTodo(long todo) { this.todo = todo; }
    public long getApproved() { return approved; }
    public void setApproved(long approved) { this.approved = approved; }
    public long getRejected() { return rejected; }
    public void setRejected(long rejected) { this.rejected = rejected; }
}
