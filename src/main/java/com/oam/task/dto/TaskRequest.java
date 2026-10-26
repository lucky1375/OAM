package com.oam.task.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 新建/编辑任务请求 */
public class TaskRequest {

    @NotBlank(message = "请填写任务标题")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @Size(max = 2000, message = "任务描述不能超过2000字")
    private String description;

    /** 负责人，不传则默认当前用户 */
    private Long assigneeUserId;

    /** LOW / NORMAL / HIGH / URGENT */
    private String priority;

    /** 截止日期 yyyy-MM-dd */
    private String dueDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
