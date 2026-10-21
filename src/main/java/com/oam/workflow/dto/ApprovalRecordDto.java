package com.oam.workflow.dto;

import com.oam.workflow.entity.ApprovalRecord;

import java.time.format.DateTimeFormatter;

public class ApprovalRecordDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String action;
    private String operatorName;
    private String targetName;
    private String comment;
    private String createdAt;

    public static ApprovalRecordDto from(ApprovalRecord r) {
        ApprovalRecordDto dto = new ApprovalRecordDto();
        dto.id = r.getId();
        dto.action = r.getAction();
        dto.operatorName = r.getOperatorName();
        dto.targetName = r.getTargetName();
        dto.comment = r.getComment();
        dto.createdAt = r.getCreatedAt() == null ? null : r.getCreatedAt().format(FMT);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
