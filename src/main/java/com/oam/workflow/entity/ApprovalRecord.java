package com.oam.workflow.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 审批操作记录：提交、通过、驳回、转办、撤回，用于审批轨迹。
 */
@Entity
@Table(name = "wf_record")
public class ApprovalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 流程ID */
    @Column(nullable = false)
    private Long flowId;

    /** 动作：SUBMIT-提交，APPROVE-通过，REJECT-驳回，DELEGATE-转办，CANCEL-撤回 */
    @Column(nullable = false, length = 20)
    private String action;

    /** 操作人用户ID */
    @Column(nullable = false)
    private Long operatorUserId;

    /** 操作人姓名 */
    @Column(length = 50)
    private String operatorName;

    /** 转办目标用户ID（仅 DELEGATE） */
    @Column
    private Long targetUserId;

    /** 转办目标姓名（仅 DELEGATE） */
    @Column(length = 50)
    private String targetName;

    /** 审批意见 */
    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
