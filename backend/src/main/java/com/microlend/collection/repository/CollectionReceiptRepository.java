package com.microlend.collection.repository;

import com.microlend.collection.enums.BorrowerApprovalStatus;
import com.microlend.collection.entity.CollectionReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionReceiptRepository extends JpaRepository<CollectionReceipt, Long> {

    List<CollectionReceipt> findByBorrowerId(Long borrowerId);

    List<CollectionReceipt> findByBorrowerIdAndBorrowerApprovalStatus(Long borrowerId, BorrowerApprovalStatus status);

    List<CollectionReceipt> findByBorrowerApprovalStatus(BorrowerApprovalStatus status);

    List<CollectionReceipt> findByLoanAccountId(Long loanAccountId);
}
