package com.microlend.collection.repository;

import com.microlend.collection.entity.CollectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRecordRepository extends JpaRepository<CollectionRecord, Long> {

    List<CollectionRecord> findByLoanAccountId(Long loanAccountId);

    List<CollectionRecord> findByCollectedById(Long collectedById);

    List<CollectionRecord> findByCentreMeetingId(Long centreMeetingId);
}
