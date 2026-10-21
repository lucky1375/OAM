package com.oam.workflow.dto;

import java.util.List;

/** 流程详情：基本字段 + 审批轨迹 */
public class ApprovalFlowDetailDto {

    private ApprovalFlowDto flow;
    private List<ApprovalRecordDto> records;

    public ApprovalFlowDetailDto() {
    }

    public ApprovalFlowDetailDto(ApprovalFlowDto flow, List<ApprovalRecordDto> records) {
        this.flow = flow;
        this.records = records;
    }

    public ApprovalFlowDto getFlow() { return flow; }
    public void setFlow(ApprovalFlowDto flow) { this.flow = flow; }
    public List<ApprovalRecordDto> getRecords() { return records; }
    public void setRecords(List<ApprovalRecordDto> records) { this.records = records; }
}
