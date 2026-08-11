package com.microlend.analytics.repository;

import com.microlend.analytics.entity.PortfolioReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioReportRepository extends JpaRepository<PortfolioReport, Long> {
}
