package com.oam.task.dto;

import com.oam.task.entity.TaskProgress;

import java.time.format.DateTimeFormatter;

/** 任务进度记录 */
public class TaskProgressDto {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private Long operatorUserId;
    private String operatorName;
    private Integer progress;
    private String status;
    private String note;
    private String createdAt;

    public static TaskProgressDto from(TaskProgress p) {
        TaskProgressDto dto = new TaskProgressDto();
        dto.id = p.getId();
        dto.operatorUserId = p.getOperatorUserId();
        dto.operatorName = p.getOperatorName();
        dto.progress = p.getProgress();
        dto.status = p.getStatus();
        dto.note = p.getNote();
        dto.createdAt = p.getCreatedAt() == null ? null : p.getCreatedAt().format(TIME_FMT);
        return dto;
    }

    public Long getId() { return id; }
    public Long getOperatorUserId() { return operatorUserId; }
    public String getOperatorName() { return operatorName; }
    public Integer getProgress() { return progress; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
}
