package com.oam.task.dto;

import com.oam.task.entity.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 任务列表/详情 */
public class TaskDto {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String title;
    private String description;
    private Long assigneeUserId;
    private String assigneeName;
    private Long creatorUserId;
    private String creatorName;
    private String priority;
    private String status;
    private Integer progress;
    private String dueDate;
    private String completedAt;
    private String createdAt;
    private String updatedAt;
    private boolean overdue;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canAssign;
    private boolean canUpdateProgress;

    public static TaskDto from(Task task, Long currentUserId, boolean admin) {
        TaskDto dto = new TaskDto();
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.description = task.getDescription();
        dto.assigneeUserId = task.getAssigneeUserId();
        dto.assigneeName = task.getAssigneeName();
        dto.creatorUserId = task.getCreatorUserId();
        dto.creatorName = task.getCreatorName();
        dto.priority = task.getPriority();
        dto.status = task.getStatus();
        dto.progress = task.getProgress() == null ? 0 : task.getProgress();
        dto.dueDate = task.getDueDate() == null ? null : task.getDueDate().format(DATE_FMT);
        dto.completedAt = format(task.getCompletedAt());
        dto.createdAt = format(task.getCreatedAt());
        dto.updatedAt = format(task.getUpdatedAt());
        dto.overdue = task.getDueDate() != null && !"DONE".equals(task.getStatus())
                && task.getDueDate().isBefore(LocalDate.now());
        boolean owner = currentUserId != null && currentUserId.equals(task.getCreatorUserId());
        boolean assignee = currentUserId != null && currentUserId.equals(task.getAssigneeUserId());
        dto.canEdit = admin || owner;
        dto.canDelete = admin || owner;
        dto.canAssign = admin || owner;
        dto.canUpdateProgress = admin || owner || assignee;
        return dto;
    }

    private static String format(LocalDateTime time) {
        return time == null ? null : time.format(TIME_FMT);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public String getAssigneeName() { return assigneeName; }
    public Long getCreatorUserId() { return creatorUserId; }
    public String getCreatorName() { return creatorName; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public Integer getProgress() { return progress; }
    public String getDueDate() { return dueDate; }
    public String getCompletedAt() { return completedAt; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public boolean isOverdue() { return overdue; }
    public boolean isCanEdit() { return canEdit; }
    public boolean isCanDelete() { return canDelete; }
    public boolean isCanAssign() { return canAssign; }
    public boolean isCanUpdateProgress() { return canUpdateProgress; }
}
