package com.oam.expense.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销发票明细：支持 OCR 识别或手工录入，保存时做基础验真校验。
 */
@Entity
@Table(name = "exp_invoice")
public class ExpenseInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属报销单 */
    @Column(nullable = false)
    private Long claimId;

    /** 发票类型：VAT_SPECIAL-增值税专用发票，VAT_NORMAL-增值税普通发票，VAT_ELECTRONIC-电子发票，OTHER-其他 */
    @Column(nullable = false, length = 20)
    private String invoiceType = "VAT_NORMAL";

    /** 发票代码 */
    @Column(length = 30)
    private String invoiceCode;

    /** 发票号码 */
    @Column(nullable = false, length = 30)
    private String invoiceNumber;

    /** 开票日期 yyyy-MM-dd */
    @Column(length = 20)
    private String invoiceDate;

    /** 销售方名称 */
    @Column(length = 100)
    private String sellerName;

    /** 购买方名称 */
    @Column(length = 100)
    private String buyerName;

    /** 不含税金额 */
    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    /** 税额 */
    @Column(precision = 14, scale = 2)
    private BigDecimal taxAmount;

    /** 价税合计 */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** 校验状态：UNVERIFIED-未校验，VERIFIED-校验通过，INVALID-校验不通过 */
    @Column(nullable = false, length = 20)
    private String verifyStatus = "UNVERIFIED";

    /** 校验说明 */
    @Column(length = 200)
    private String verifyMessage;

    /** 数据来源：MANUAL-手工录入，OCR-发票识别 */
    @Column(nullable = false, length = 20)
    private String source = "MANUAL";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
