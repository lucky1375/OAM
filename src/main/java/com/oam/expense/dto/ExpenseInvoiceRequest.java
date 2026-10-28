package com.oam.expense.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/** 报销单中的单张发票 */
public class ExpenseInvoiceRequest {

    /** 发票类型：VAT_SPECIAL / VAT_NORMAL / VAT_ELECTRONIC / OTHER */
    private String invoiceType;

    @Size(max = 30, message = "发票代码不能超过30位")
    private String invoiceCode;

    @NotBlank(message = "请填写发票号码")
    @Size(max = 30, message = "发票号码不能超过30位")
    private String invoiceNumber;

    /** 开票日期 yyyy-MM-dd */
    private String invoiceDate;

    @Size(max = 100, message = "销售方名称不能超过100字")
    private String sellerName;

    @Size(max = 100, message = "购买方名称不能超过100字")
    private String buyerName;

    /** 不含税金额 */
    @DecimalMin(value = "0", message = "不含税金额不能为负数")
    private BigDecimal amount;

    /** 税额 */
    @DecimalMin(value = "0", message = "税额不能为负数")
    private BigDecimal taxAmount;

    @NotNull(message = "请填写价税合计")
    @DecimalMin(value = "0.01", message = "价税合计必须大于0")
    private BigDecimal totalAmount;

    /** 数据来源：MANUAL / OCR */
    private String source;

    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
