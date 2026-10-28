package com.oam.expense.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/** 新建/编辑报销单 */
public class ExpenseClaimRequest {

    @NotBlank(message = "请填写报销事由")
    @Size(max = 100, message = "报销事由不能超过100字")
    private String title;

    /** 费用分类：TRAVEL / OFFICE / MEETING / OTHER */
    private String category;

    @Size(max = 1000, message = "报销说明不能超过1000字")
    private String description;

    @Valid
    private List<ExpenseInvoiceRequest> invoices;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<ExpenseInvoiceRequest> getInvoices() { return invoices; }
    public void setInvoices(List<ExpenseInvoiceRequest> invoices) { this.invoices = invoices; }
}
