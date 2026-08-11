package com.microlend.grouporigination.entity;


import com.microlend.grouporigination.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "borrower_group", indexes = {
        @Index(name = "idx_group_centre", columnList = "centre_id"),
        @Index(name = "idx_group_officer", columnList = "created_by_field_officer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 120)
    private String groupName;

    @Column(name = "centre_id", nullable = false)
    private Long centreId;

    @Column(name = "created_by_field_officer_id", nullable = false)
    private Long createdByFieldOfficerId;

    @Column(name = "formation_date", nullable = false)
    private LocalDate formationDate;

    @Column(name = "member_count", nullable = false)
    @Builder.Default
    private Integer memberCount = 0;

    @Column(name = "joint_liability_enabled", nullable = false)
    @Builder.Default
    private boolean jointLiabilityEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;
}
