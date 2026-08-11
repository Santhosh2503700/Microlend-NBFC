package com.microlend.borrower.repository;

import com.microlend.borrower.entity.CreditAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditAssessmentRepository extends JpaRepository<CreditAssessment, Long> {

    List<CreditAssessment> findByBorrowerId(Long borrowerId);

    Optional<CreditAssessment> findFirstByApplicationIdOrderByAssessmentDateDesc(Long applicationId);
}
