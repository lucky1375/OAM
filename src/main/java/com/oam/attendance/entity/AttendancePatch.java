package com.oam.attendance.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 补卡申请。
 */
@Entity
@Table(name = "att_patch")
public class AttendancePatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(nullable = false)
    private LocalDate workDate;

    /** IN 上班卡 / OUT 下班卡 */
    @Column(nullable = false, length = 10)
    private String patchType;

    @Column(nullable = false)
    private LocalDateTime patchTime;

    /** 关联的审批流程 ID（wf_flow.id），补卡申请统一走流程审批 */
    @Column
    private Long flowId;

    @Column(nullable = false, length = 200)
    private String reason;

    /** PENDING / APPROVED / REJECTED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 50)
    private String approverName;

    @Column(length = 200)
    private String approveComment;

    private LocalDateTime approvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public String getPatchType() { return patchType; }
    public void setPatchType(String patchType) { this.patchType = patchType; }
    public LocalDateTime getPatchTime() { return patchTime; }
    public void setPatchTime(LocalDateTime patchTime) { this.patchTime = patchTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getApproveComment() { return approveComment; }
    public void setApproveComment(String approveComment) { this.approveComment = approveComment; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
