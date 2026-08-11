package com.microlend.loan.repository;

import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.enums.LoanAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    List<LoanAccount> findByBorrowerId(Long borrowerId);

    List<LoanAccount> findByBorrowerIdAndStatus(Long borrowerId, LoanAccountStatus status);

    List<LoanAccount> findByStatus(LoanAccountStatus status);
}
