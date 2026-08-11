package com.microlend.loan.dto;

import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentScheduleResponse(
        Long scheduleId,
        Long loanAccountId,
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal principalDue,
        BigDecimal interestDue,
        BigDecimal totalDue,
        ScheduleStatus status
) {
    public static RepaymentScheduleResponse from(RepaymentSchedule s) {
        return new RepaymentScheduleResponse(s.getScheduleId(), s.getLoanAccountId(),
                s.getInstallmentNumber(), s.getDueDate(), s.getPrincipalDue(), s.getInterestDue(),
                s.getTotalDue(), s.getStatus());
    }
}
