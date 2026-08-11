package com.microlend.delinquency.repository;

import com.microlend.delinquency.enums.CaseStatus;
import com.microlend.delinquency.entity.DelinquencyCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DelinquencyCaseRepository extends JpaRepository<DelinquencyCase, Long> {

    Optional<DelinquencyCase> findFirstByLoanAccountIdAndStatusNot(Long loanAccountId, CaseStatus status);

    List<DelinquencyCase> findByLoanAccountId(Long loanAccountId);

    List<DelinquencyCase> findByAssignedCollectionsOfficerId(Long officerId);

    List<DelinquencyCase> findByAssignedCollectionsOfficerIdAndStatusNot(Long officerId, CaseStatus status);

    List<DelinquencyCase> findByStatusNot(CaseStatus status);
}
