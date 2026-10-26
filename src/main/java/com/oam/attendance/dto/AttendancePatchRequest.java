package com.oam.attendance.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AttendancePatchRequest {

    @NotBlank(message = "请选择补卡日期")
    private String workDate;

    /** IN 上班卡 / OUT 下班卡 */
    @NotBlank(message = "请选择补卡类型")
    private String patchType;

    /** 补卡时间，格式 HH:mm */
    @NotBlank(message = "请选择补卡时间")
    private String patchTime;

    @NotBlank(message = "请填写补卡原因")
    @Size(max = 200, message = "补卡原因不能超过200字")
    private String reason;

    /** 审批人 ID：该类型未配置固定审批节点时必填 */
    private Long approverUserId;

    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }
    public String getPatchType() { return patchType; }
    public void setPatchType(String patchType) { this.patchType = patchType; }
    public String getPatchTime() { return patchTime; }
    public void setPatchTime(String patchTime) { this.patchTime = patchTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
}
