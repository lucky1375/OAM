package com.oam.expense.dto;

import com.oam.expense.entity.ExpenseInvoice;

import java.math.BigDecimal;

public class ExpenseInvoiceDto {

    private Long id;
    private String invoiceType;
    private String invoiceCode;
    private String invoiceNumber;
    private String invoiceDate;
    private String sellerName;
    private String buyerName;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String verifyStatus;
    private String verifyMessage;
    private String source;

    public static ExpenseInvoiceDto from(ExpenseInvoice i) {
        ExpenseInvoiceDto dto = new ExpenseInvoiceDto();
        dto.id = i.getId();
        dto.invoiceType = i.getInvoiceType();
        dto.invoiceCode = i.getInvoiceCode();
        dto.invoiceNumber = i.getInvoiceNumber();
        dto.invoiceDate = i.getInvoiceDate();
        dto.sellerName = i.getSellerName();
        dto.buyerName = i.getBuyerName();
        dto.amount = i.getAmount();
        dto.taxAmount = i.getTaxAmount();
        dto.totalAmount = i.getTotalAmount();
        dto.verifyStatus = i.getVerifyStatus();
        dto.verifyMessage = i.getVerifyMessage();
        dto.source = i.getSource();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(String verifyStatus) { this.verifyStatus = verifyStatus; }
    public String getVerifyMessage() { return verifyMessage; }
    public void setVerifyMessage(String verifyMessage) { this.verifyMessage = verifyMessage; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
