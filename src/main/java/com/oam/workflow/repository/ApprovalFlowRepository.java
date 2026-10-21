package com.oam.workflow.repository;

import com.oam.workflow.entity.ApprovalFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalFlowRepository extends JpaRepository<ApprovalFlow, Long> {

    List<ApprovalFlow> findAllByOrderByCreatedAtDesc();

    List<ApprovalFlow> findByApplicantUserIdOrderByCreatedAtDesc(Long applicantUserId);

    List<ApprovalFlow> findByApproverUserIdAndStatusOrderByCreatedAtDesc(Long approverUserId, String status);

    List<ApprovalFlow> findByApproverUserIdOrderByCreatedAtDesc(Long approverUserId);

    long countByApplicantUserIdAndStatus(Long applicantUserId, String status);

    long countByApproverUserIdAndStatus(Long approverUserId, String status);
}
