package com.microlend.grouporigination.repository;

import com.microlend.grouporigination.entity.Centre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentreRepository extends JpaRepository<Centre, Long> {

    List<Centre> findByCreatedByFieldOfficerId(Long fieldOfficerId);

    List<Centre> findByBranchId(Long branchId);
}
