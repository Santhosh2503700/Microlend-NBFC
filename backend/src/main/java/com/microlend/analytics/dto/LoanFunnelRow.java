package com.microlend.analytics.dto;

//Count of applications at one lifecycle status.

public record LoanFunnelRow(String status, long count) {
}
