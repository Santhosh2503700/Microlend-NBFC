package com.microlend.loan.repository;

import com.microlend.loan.enums.ApplicationStatus;
import com.microlend.loan.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByBorrowerId(Long borrowerId);

    List<LoanApplication> findByBorrowerIdIn(List<Long> borrowerIds);

    List<LoanApplication> findByCreditOfficerId(Long creditOfficerId);

    List<LoanApplication> findByStatus(ApplicationStatus status);
}
