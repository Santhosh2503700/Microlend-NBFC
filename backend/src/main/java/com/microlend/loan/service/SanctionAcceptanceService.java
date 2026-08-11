package com.microlend.loan.service;

import com.microlend.loan.dto.SanctionLetterResponse;

import java.util.List;


public interface SanctionAcceptanceService {

    List<SanctionLetterResponse> listForBorrower(Long borrowerUserId);

    Object accept(Long borrowerUserId, Long sanctionId);

    SanctionLetterResponse reject(Long borrowerUserId, Long sanctionId);
}
