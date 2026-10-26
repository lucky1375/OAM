package com.oam.task.dto;

import java.util.List;

/** 任务详情（含进度时间线） */
public class TaskDetailDto {

    private TaskDto task;
    private List<TaskProgressDto> records;

    public TaskDetailDto(TaskDto task, List<TaskProgressDto> records) {
        this.task = task;
        this.records = records;
    }

    public TaskDto getTask() { return task; }
    public List<TaskProgressDto> getRecords() { return records; }
}
