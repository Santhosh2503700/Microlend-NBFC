package com.microlend.grouporigination.repository;

import com.microlend.grouporigination.entity.BorrowerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowerGroupRepository extends JpaRepository<BorrowerGroup, Long> {

    List<BorrowerGroup> findByCreatedByFieldOfficerId(Long fieldOfficerId);

    List<BorrowerGroup> findByCentreId(Long centreId);
}
