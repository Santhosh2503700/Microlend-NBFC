package com.microlend.loan.repository;

import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {

    List<LoanProduct> findByStatus(ProductStatus status);
}
