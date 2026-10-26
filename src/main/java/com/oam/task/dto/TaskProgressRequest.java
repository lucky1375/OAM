package com.oam.task.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 进度更新请求 */
public class TaskProgressRequest {

    @NotNull(message = "请填写进度")
    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    private Integer progress;

    /** 可选：TODO / IN_PROGRESS / DONE，不传时按进度自动推导 */
    private String status;

    @Size(max = 500, message = "进度说明不能超过500字")
    private String note;

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
