package com.microlend.loan.repository;

import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {

    List<RepaymentSchedule> findByLoanAccountIdOrderByInstallmentNumberAsc(Long loanAccountId);

    List<RepaymentSchedule> findByLoanAccountIdAndStatus(Long loanAccountId, ScheduleStatus status);

    // Used by the Phase 6 delinquency scheduler to find overdue installments.
    List<RepaymentSchedule> findByStatusAndDueDateBefore(ScheduleStatus status, LocalDate date);
}
