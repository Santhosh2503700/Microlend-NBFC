package com.microlend.borrower.dto;

import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.enums.BorrowerStatus;
import com.microlend.borrower.enums.BorrowerType;
import com.microlend.borrower.enums.Gender;
import com.microlend.common.MaskingUtil;

import java.math.BigDecimal;
import java.time.LocalDate;


public record BorrowerResponse(
        Long borrowerId,
        String name,
        LocalDate dateOfBirth,
        Gender gender,
        String nationalIdNumberMasked,
        String village,
        String district,
        String phone,
        String occupation,
        BigDecimal monthlyIncome,
        String bankAccountNumberMasked,
        String ifscCode,
        BorrowerStatus status,
        BorrowerType borrowerType,
        Long centreId,
        Long groupId,
        Long portalUserId,
        Long registeredByFieldOfficerId,
        String registeredByFieldOfficerName
) {
    public static BorrowerResponse from(Borrower b, String officerName) {
        return new BorrowerResponse(
                b.getBorrowerId(), b.getName(), b.getDateOfBirth(), b.getGender(),
                MaskingUtil.maskAadhaar(b.getNationalIdNumber()), b.getVillage(), b.getDistrict(),
                b.getPhone(), b.getOccupation(), b.getMonthlyIncome(),
                MaskingUtil.maskAccount(b.getBankAccountNumber()), b.getIfscCode(),
                b.getStatus(), b.getBorrowerType(), b.getCentreId(), b.getGroupId(),
                b.getPortalUserId(), b.getRegisteredByFieldOfficerId(), officerName
        );
    }
}
