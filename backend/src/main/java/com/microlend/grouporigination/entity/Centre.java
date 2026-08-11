package com.microlend.grouporigination.entity;


import com.microlend.grouporigination.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;


@Entity
@Table(name = "centre", indexes = {
        @Index(name = "idx_centre_officer", columnList = "created_by_field_officer_id"),
        @Index(name = "idx_centre_branch", columnList = "branch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "centre_id")
    private Long centreId;

    @Column(name = "centre_name", nullable = false, length = 120)
    private String centreName;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "created_by_field_officer_id", nullable = false)
    private Long createdByFieldOfficerId;

    @Column(name = "village", nullable = false, length = 120)
    private String village;

    @Column(name = "meeting_day", length = 12)
    private String meetingDay;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;
}
