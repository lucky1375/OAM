package com.oam.task.dto;

/** 任务分派请求 */
public class TaskAssignRequest {

    private Long assigneeUserId;

    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
}
