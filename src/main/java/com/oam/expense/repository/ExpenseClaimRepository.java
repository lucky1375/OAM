package com.oam.expense.repository;

import com.oam.expense.entity.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {

    List<ExpenseClaim> findByApplicantUserIdOrderByCreatedAtDesc(Long applicantUserId);

    List<ExpenseClaim> findByFlowIdInOrderByCreatedAtDesc(Collection<Long> flowIds);

    List<ExpenseClaim> findAllByOrderByCreatedAtDesc();

    Optional<ExpenseClaim> findByFlowId(Long flowId);

    long countByApplicantUserIdAndStatus(Long applicantUserId, String status);

    @Query("select coalesce(sum(c.amount), 0) from ExpenseClaim c "
            + "where c.applicantUserId = :applicantUserId and c.status = 'APPROVED'")
    BigDecimal sumApprovedAmount(@Param("applicantUserId") Long applicantUserId);
}
