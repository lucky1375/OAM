package com.oam.expense.dto;

import com.oam.expense.entity.ExpenseClaim;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ExpenseClaimDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String claimNo;
    private String title;
    private String category;
    private BigDecimal amount;
    private Integer invoiceCount;
    private String description;
    private String status;
    private Long applicantUserId;
    private String applicantName;
    private String currentApproverName;
    private String submittedAt;
    private String finishedAt;
    private String createdAt;
    private Boolean mine;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canSubmit;
    private Boolean canCancel;
    private Boolean canApprove;

    public static ExpenseClaimDto from(ExpenseClaim c, Long currentUserId) {
        ExpenseClaimDto dto = new ExpenseClaimDto();
        dto.id = c.getId();
        dto.claimNo = String.format("EXP%06d", c.getId());
        dto.title = c.getTitle();
        dto.category = c.getCategory();
        dto.amount = c.getAmount();
        dto.invoiceCount = c.getInvoiceCount();
        dto.description = c.getDescription();
        dto.status = c.getStatus();
        dto.applicantUserId = c.getApplicantUserId();
        dto.applicantName = c.getApplicantName();
        dto.submittedAt = c.getSubmittedAt() == null ? null : c.getSubmittedAt().format(FMT);
        dto.finishedAt = c.getFinishedAt() == null ? null : c.getFinishedAt().format(FMT);
        dto.createdAt = c.getCreatedAt() == null ? null : c.getCreatedAt().format(FMT);
        dto.mine = currentUserId != null && currentUserId.equals(c.getApplicantUserId());
        boolean draft = "DRAFT".equals(c.getStatus());
        dto.canEdit = Boolean.TRUE.equals(dto.mine) && draft;
        dto.canDelete = Boolean.TRUE.equals(dto.mine) && ("DRAFT".equals(c.getStatus())
                || "REJECTED".equals(c.getStatus()) || "CANCELLED".equals(c.getStatus()));
        dto.canSubmit = Boolean.TRUE.equals(dto.mine) && draft;
        dto.canCancel = Boolean.TRUE.equals(dto.mine) && "PENDING".equals(c.getStatus());
        dto.canApprove = false;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String claimNo) { this.claimNo = claimNo; }
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
    public String getCurrentApproverName() { return currentApproverName; }
    public void setCurrentApproverName(String currentApproverName) { this.currentApproverName = currentApproverName; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getFinishedAt() { return finishedAt; }
    public void setFinishedAt(String finishedAt) { this.finishedAt = finishedAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Boolean getMine() { return mine; }
    public void setMine(Boolean mine) { this.mine = mine; }
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean canDelete) { this.canDelete = canDelete; }
    public Boolean getCanSubmit() { return canSubmit; }
    public void setCanSubmit(Boolean canSubmit) { this.canSubmit = canSubmit; }
    public Boolean getCanCancel() { return canCancel; }
    public void setCanCancel(Boolean canCancel) { this.canCancel = canCancel; }
    public Boolean getCanApprove() { return canApprove; }
    public void setCanApprove(Boolean canApprove) { this.canApprove = canApprove; }
}
