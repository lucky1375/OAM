package com.oam.workflow.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 审批流程实例：请假/报销/采购等申请单及其当前审批状态。
 */
@Entity
@Table(name = "wf_flow")
public class ApprovalFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 流程类型：LEAVE-请假，EXPENSE-报销，PURCHASE-采购，OTHER-其他 */
    @Column(nullable = false, length = 20)
    private String type = "OTHER";

    /** 标题 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 申请内容/事由 */
    @Column(nullable = false, length = 2000)
    private String content;

    /** 申请人用户ID */
    @Column(nullable = false)
    private Long applicantUserId;

    /** 申请人姓名 */
    @Column(length = 50)
    private String applicantName;

    /** 当前审批人用户ID */
    @Column
    private Long approverUserId;

    /** 当前审批人姓名 */
    @Column(length = 50)
    private String approverName;

    /** 状态：PENDING-审批中，APPROVED-已通过，REJECTED-已驳回，CANCELLED-已撤回 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** Activiti 流程实例ID */
    @Column(length = 64)
    private String processInstanceId;

    /** 审批节点名称序列（逗号分隔，提交时快照） */
    @Column(length = 1000)
    private String nodeSequence;

    /** 审批人账号序列（逗号分隔，提交时快照） */
    @Column(length = 1000)
    private String approverSequence;

    /** 当前节点序号，从 0 开始 */
    @Column
    private Integer nodeIndex = 0;

    /** 审批节点总数 */
    @Column
    private Integer totalNodes = 1;

    /** 当前节点名称 */
    @Column(length = 100)
    private String currentNodeName;

    /** 请假开始时间（仅请假流程） */
    @Column
    private LocalDateTime leaveStartTime;

    /** 请假结束时间（仅请假流程） */
    @Column
    private LocalDateTime leaveEndTime;

    /** 完结时间（通过/驳回/撤回） */
    @Column
    private LocalDateTime finishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long applicantUserId) { this.applicantUserId = applicantUserId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getNodeSequence() { return nodeSequence; }
    public void setNodeSequence(String nodeSequence) { this.nodeSequence = nodeSequence; }
    public String getApproverSequence() { return approverSequence; }
    public void setApproverSequence(String approverSequence) { this.approverSequence = approverSequence; }
    public Integer getNodeIndex() { return nodeIndex; }
    public void setNodeIndex(Integer nodeIndex) { this.nodeIndex = nodeIndex; }
    public Integer getTotalNodes() { return totalNodes; }
    public void setTotalNodes(Integer totalNodes) { this.totalNodes = totalNodes; }
    public String getCurrentNodeName() { return currentNodeName; }
    public void setCurrentNodeName(String currentNodeName) { this.currentNodeName = currentNodeName; }
    public LocalDateTime getLeaveStartTime() { return leaveStartTime; }
    public void setLeaveStartTime(LocalDateTime leaveStartTime) { this.leaveStartTime = leaveStartTime; }
    public LocalDateTime getLeaveEndTime() { return leaveEndTime; }
    public void setLeaveEndTime(LocalDateTime leaveEndTime) { this.leaveEndTime = leaveEndTime; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
