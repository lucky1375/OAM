package com.oam.expense.repository;

import com.oam.expense.entity.ExpenseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseInvoiceRepository extends JpaRepository<ExpenseInvoice, Long> {

    List<ExpenseInvoice> findByClaimIdOrderByIdAsc(Long claimId);

    void deleteByClaimId(Long claimId);

    /** 统计发票号码是否已存在于其他审批中/已通过的报销单（防止重复报销） */
    @Query("select count(i) from ExpenseInvoice i, ExpenseClaim c "
            + "where i.claimId = c.id and i.invoiceNumber = :invoiceNumber "
            + "and c.status in ('PENDING', 'APPROVED') and c.id <> :excludeClaimId")
    long countDuplicate(@Param("invoiceNumber") String invoiceNumber,
                        @Param("excludeClaimId") Long excludeClaimId);

    /** 统计发票号码是否已存在于审批中/已通过的报销单 */
    @Query("select count(i) from ExpenseInvoice i, ExpenseClaim c "
            + "where i.claimId = c.id and i.invoiceNumber = :invoiceNumber "
            + "and c.status in ('PENDING', 'APPROVED')")
    long countDuplicateInUse(@Param("invoiceNumber") String invoiceNumber);
}
