package com.oam.workflow.dto;

import com.oam.workflow.entity.ApprovalFlow;

import java.time.format.DateTimeFormatter;

public class ApprovalFlowDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String flowNo;
    private String type;
    private String title;
    private String content;
    private String applicantName;
    private String approverName;
    private String status;
    private Integer currentStep;
    private Integer totalNodes;
    private String currentNodeName;
    private Boolean canApprove;
    private Boolean canCancel;
    private Boolean mine;
    private String createdAt;
    private String finishedAt;
    private String leaveStartTime;
    private String leaveEndTime;

    public static ApprovalFlowDto from(ApprovalFlow f, Long currentUserId) {
        ApprovalFlowDto dto = new ApprovalFlowDto();
        dto.id = f.getId();
        dto.flowNo = String.format("WF%06d", f.getId());
        dto.type = f.getType();
        dto.title = f.getTitle();
        dto.content = f.getContent();
        dto.applicantName = f.getApplicantName();
        dto.approverName = f.getApproverName();
        dto.status = f.getStatus();
        dto.currentStep = (f.getNodeIndex() == null ? 0 : f.getNodeIndex()) + 1;
        dto.totalNodes = f.getTotalNodes() == null ? 1 : f.getTotalNodes();
        dto.currentNodeName = f.getCurrentNodeName();
        dto.mine = currentUserId != null && currentUserId.equals(f.getApplicantUserId());
        dto.canApprove = "PENDING".equals(f.getStatus())
                && currentUserId != null && currentUserId.equals(f.getApproverUserId());
        dto.canCancel = "PENDING".equals(f.getStatus()) && Boolean.TRUE.equals(dto.mine);
        dto.createdAt = f.getCreatedAt() == null ? null : f.getCreatedAt().format(FMT);
        dto.finishedAt = f.getFinishedAt() == null ? null : f.getFinishedAt().format(FMT);
        dto.leaveStartTime = f.getLeaveStartTime() == null ? null : f.getLeaveStartTime().format(FMT);
        dto.leaveEndTime = f.getLeaveEndTime() == null ? null : f.getLeaveEndTime().format(FMT);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlowNo() { return flowNo; }
    public void setFlowNo(String flowNo) { this.flowNo = flowNo; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }
    public Integer getTotalNodes() { return totalNodes; }
    public void setTotalNodes(Integer totalNodes) { this.totalNodes = totalNodes; }
    public String getCurrentNodeName() { return currentNodeName; }
    public void setCurrentNodeName(String currentNodeName) { this.currentNodeName = currentNodeName; }
    public Boolean getCanApprove() { return canApprove; }
    public void setCanApprove(Boolean canApprove) { this.canApprove = canApprove; }
    public Boolean getCanCancel() { return canCancel; }
    public void setCanCancel(Boolean canCancel) { this.canCancel = canCancel; }
    public Boolean getMine() { return mine; }
    public void setMine(Boolean mine) { this.mine = mine; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getFinishedAt() { return finishedAt; }
    public void setFinishedAt(String finishedAt) { this.finishedAt = finishedAt; }
    public String getLeaveStartTime() { return leaveStartTime; }
    public void setLeaveStartTime(String leaveStartTime) { this.leaveStartTime = leaveStartTime; }
    public String getLeaveEndTime() { return leaveEndTime; }
    public void setLeaveEndTime(String leaveEndTime) { this.leaveEndTime = leaveEndTime; }
}
