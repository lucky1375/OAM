package com.oam.attendance.dto;

import com.oam.attendance.entity.AttendancePatch;

import java.time.format.DateTimeFormatter;

public class AttendancePatchDto {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private Long id;
    private Long userId;
    private String username;
    private String userName;
    private String workDate;
    private String patchType;
    private String patchTime;
    private String reason;
    private String status;
    private Long flowId;
    private String approverName;
    private String approveComment;
    private String approvedAt;
    private String createdAt;

    public static AttendancePatchDto from(AttendancePatch p) {
        AttendancePatchDto dto = new AttendancePatchDto();
        dto.id = p.getId();
        dto.userId = p.getUserId();
        dto.username = p.getUsername();
        dto.userName = p.getUserName();
        dto.workDate = p.getWorkDate() == null ? null : p.getWorkDate().format(DATE_FMT);
        dto.patchType = p.getPatchType();
        dto.patchTime = p.getPatchTime() == null ? null : p.getPatchTime().format(TIME_FMT);
        dto.reason = p.getReason();
        dto.status = p.getStatus();
        dto.flowId = p.getFlowId();
        dto.approverName = p.getApproverName();
        dto.approveComment = p.getApproveComment();
        dto.approvedAt = p.getApprovedAt() == null ? null
                : p.getApprovedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        dto.createdAt = p.getCreatedAt() == null ? null
                : p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }
    public String getPatchType() { return patchType; }
    public void setPatchType(String patchType) { this.patchType = patchType; }
    public String getPatchTime() { return patchTime; }
    public void setPatchTime(String patchTime) { this.patchTime = patchTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getApproveComment() { return approveComment; }
    public void setApproveComment(String approveComment) { this.approveComment = approveComment; }
    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
