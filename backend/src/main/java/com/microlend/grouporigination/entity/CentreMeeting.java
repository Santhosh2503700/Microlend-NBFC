package com.microlend.grouporigination.entity;


import com.microlend.grouporigination.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "centre_meeting", indexes = {
        @Index(name = "idx_meeting_centre", columnList = "centre_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentreMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    @Column(name = "centre_id", nullable = false)
    private Long centreId;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "conducted_by_id", nullable = false)
    private Long conductedById;

    @Column(name = "attendance_count")
    private Integer attendanceCount;

    // Monetary field: CHECK (collection_amount >= 0)
    @Column(name = "collection_amount", precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (collection_amount >= 0)")
    private BigDecimal collectionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;
}
