package com.oam.expense.dto;

/** 发票识别服务能力状态 */
public class OcrStatusDto {

    private boolean enabled;
    private String provider;

    public OcrStatusDto() {
    }

    public OcrStatusDto(boolean enabled, String provider) {
        this.enabled = enabled;
        this.provider = provider;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}
