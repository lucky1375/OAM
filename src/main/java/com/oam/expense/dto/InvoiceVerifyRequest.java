package com.oam.expense.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/** 发票校验请求：用于录入时即时校验票据是否可用 */
public class InvoiceVerifyRequest {

    /** 当前报销单 ID（编辑已有报销单时传入，用于排除自身） */
    private Long claimId;

    @NotNull(message = "请填写发票信息")
    @Valid
    private ExpenseInvoiceRequest invoice;

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }
    public ExpenseInvoiceRequest getInvoice() { return invoice; }
    public void setInvoice(ExpenseInvoiceRequest invoice) { this.invoice = invoice; }
}
