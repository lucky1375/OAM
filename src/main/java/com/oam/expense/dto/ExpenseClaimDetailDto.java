package com.oam.expense.dto;

import com.oam.workflow.dto.ApprovalFlowDto;
import com.oam.workflow.dto.ApprovalRecordDto;

import java.util.List;

/** 报销单详情：基本信息 + 发票明细 + 审批流程与轨迹 */
public class ExpenseClaimDetailDto {

    private ExpenseClaimDto claim;
    private List<ExpenseInvoiceDto> invoices;
    private ApprovalFlowDto flow;
    private List<ApprovalRecordDto> records;

    public ExpenseClaimDetailDto() {
    }

    public ExpenseClaimDetailDto(ExpenseClaimDto claim, List<ExpenseInvoiceDto> invoices,
                                 ApprovalFlowDto flow, List<ApprovalRecordDto> records) {
        this.claim = claim;
        this.invoices = invoices;
        this.flow = flow;
        this.records = records;
    }

    public ExpenseClaimDto getClaim() { return claim; }
    public void setClaim(ExpenseClaimDto claim) { this.claim = claim; }
    public List<ExpenseInvoiceDto> getInvoices() { return invoices; }
    public void setInvoices(List<ExpenseInvoiceDto> invoices) { this.invoices = invoices; }
    public ApprovalFlowDto getFlow() { return flow; }
    public void setFlow(ApprovalFlowDto flow) { this.flow = flow; }
    public List<ApprovalRecordDto> getRecords() { return records; }
    public void setRecords(List<ApprovalRecordDto> records) { this.records = records; }
}
