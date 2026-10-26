package com.oam.task.dto;

/** 当前用户任务概览 */
public class TaskStatsDto {

    private int todo;
    private int inProgress;
    private int done;
    private int overdue;

    public int getTodo() { return todo; }
    public void setTodo(int todo) { this.todo = todo; }
    public int getInProgress() { return inProgress; }
    public void setInProgress(int inProgress) { this.inProgress = inProgress; }
    public int getDone() { return done; }
    public void setDone(int done) { this.done = done; }
    public int getOverdue() { return overdue; }
    public void setOverdue(int overdue) { this.overdue = overdue; }
}
