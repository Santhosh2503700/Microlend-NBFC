package com.microlend.borrower.repository;

import com.microlend.borrower.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {


    List<Borrower> findByRegisteredByFieldOfficerId(Long fieldOfficerId);

    Optional<Borrower> findByPortalUserId(Long portalUserId);

    boolean existsByNationalIdNumber(String nationalIdNumber);

    boolean existsByPhone(String phone);

    List<Borrower> findByGroupId(Long groupId);

    List<Borrower> findByCentreId(Long centreId);


    List<Borrower> findByNameStartingWith(String prefix);
}