package com.microlend.delinquency.dto;


public record ScanResultResponse(
        int installmentsMarkedOverdue,
        int accountsScanned,
        int casesOpened,
        int casesUpdated,
        int casesResolved
) {
}
