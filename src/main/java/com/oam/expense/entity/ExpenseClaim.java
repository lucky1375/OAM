package com.oam.expense.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销单：一张报销单包含若干张发票，提交后统一走流程审批。
 */
@Entity
@Table(name = "exp_claim")
public class ExpenseClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 报销事由 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 费用分类：TRAVEL-差旅费，OFFICE-办公费，MEETING-会议费，OTHER-其他 */
    @Column(nullable = false, length = 20)
    private String category = "OTHER";

    /** 报销金额（发票价税合计之和） */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    /** 发票张数 */
    @Column(nullable = false)
    private Integer invoiceCount = 0;

    /** 报销说明 */
    @Column(length = 1000)
    private String description;

    /** 状态：DRAFT-草稿，PENDING-审批中，APPROVED-已通过，REJECTED-已驳回，CANCELLED-已撤回 */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    /** 申请人用户ID */
    @Column(nullable = false)
    private Long applicantUserId;

    /** 申请人姓名 */
    @Column(length = 50)
    private String applicantName;

    /** 关联的审批流程 ID（wf_flow.id） */
    @Column
    private Long flowId;

    /** 提交审批时间 */
    @Column
    private LocalDateTime submittedAt;

    /** 审批完结时间（通过/驳回/撤回） */
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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getInvoiceCount() { return invoiceCount; }
    public void setInvoiceCount(Integer invoiceCount) { this.invoiceCount = invoiceCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long applicantUserId) { this.applicantUserId = applicantUserId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
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
