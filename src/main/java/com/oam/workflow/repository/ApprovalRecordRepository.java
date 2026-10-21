package com.oam.workflow.repository;

import com.oam.workflow.entity.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {

    List<ApprovalRecord> findByFlowIdOrderByCreatedAtAsc(Long flowId);

    List<ApprovalRecord> findByOperatorUserIdOrderByCreatedAtDesc(Long operatorUserId);

    void deleteByFlowId(Long flowId);
}
