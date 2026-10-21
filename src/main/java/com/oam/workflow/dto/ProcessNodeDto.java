package com.oam.workflow.dto;

import com.oam.workflow.entity.ProcessNode;

public class ProcessNodeDto {

    private Long id;
    private String type;
    private Integer nodeOrder;
    private String nodeName;
    private Long approverUserId;
    private String approverName;

    public static ProcessNodeDto from(ProcessNode n) {
        ProcessNodeDto dto = new ProcessNodeDto();
        dto.id = n.getId();
        dto.type = n.getType();
        dto.nodeOrder = n.getNodeOrder();
        dto.nodeName = n.getNodeName();
        dto.approverUserId = n.getApproverUserId();
        dto.approverName = n.getApproverName();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getNodeOrder() { return nodeOrder; }
    public void setNodeOrder(Integer nodeOrder) { this.nodeOrder = nodeOrder; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
}
