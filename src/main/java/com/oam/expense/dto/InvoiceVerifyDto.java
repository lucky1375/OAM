package com.oam.expense.dto;

/** 发票校验结果 */
public class InvoiceVerifyDto {

    /** VERIFIED-校验通过，INVALID-校验不通过 */
    private String status;
    private String message;

    public InvoiceVerifyDto() {
    }

    public InvoiceVerifyDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
