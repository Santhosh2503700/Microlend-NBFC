package com.microlend.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "portfolio_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "scope", nullable = false, length = 60)
    private String scope;

    @Lob
    @Column(name = "metrics", columnDefinition = "TEXT")
    private String metrics;

    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    @PrePersist
    void onCreate() {
        if (generatedDate == null) {
            generatedDate = LocalDateTime.now();
        }
    }
}
