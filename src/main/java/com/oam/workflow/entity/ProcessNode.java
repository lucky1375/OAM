package com.oam.workflow.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 固定审批节点配置：按流程类型配置有序的审批环节及固定审批人。
 */
@Entity
@Table(name = "wf_process_node")
public class ProcessNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 流程类型：LEAVE / EXPENSE / PURCHASE / OTHER */
    @Column(nullable = false, length = 20)
    private String type;

    /** 节点顺序，从 0 开始 */
    @Column(nullable = false)
    private Integer nodeOrder;

    /** 节点名称，如：直属主管审批 */
    @Column(nullable = false, length = 50)
    private String nodeName;

    /** 固定审批人用户ID */
    @Column(nullable = false)
    private Long approverUserId;

    /** 固定审批人姓名 */
    @Column(length = 50)
    private String approverName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdBy;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
