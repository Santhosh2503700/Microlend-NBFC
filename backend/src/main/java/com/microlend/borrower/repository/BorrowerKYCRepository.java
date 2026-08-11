package com.microlend.borrower.repository;

import com.microlend.borrower.entity.BorrowerKYC;
import com.microlend.borrower.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowerKYCRepository extends JpaRepository<BorrowerKYC, Long> {

    List<BorrowerKYC> findByBorrowerId(Long borrowerId);

    long countByBorrowerIdAndStatus(Long borrowerId, KycStatus status);
}
