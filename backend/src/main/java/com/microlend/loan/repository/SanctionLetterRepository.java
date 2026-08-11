package com.microlend.loan.repository;

import com.microlend.loan.entity.SanctionLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SanctionLetterRepository extends JpaRepository<SanctionLetter, Long> {

    Optional<SanctionLetter> findByApplicationId(Long applicationId);

    List<SanctionLetter> findByApplicationIdIn(List<Long> applicationIds);
}
