package com.microlend.loan.service;

import com.microlend.identity.enums.Role;
import com.microlend.loan.dto.AssessmentResponse;
import com.microlend.loan.dto.DecisionRequest;
import com.microlend.loan.dto.LoanApplicationRequest;
import com.microlend.loan.dto.LoanApplicationResponse;

import java.util.List;
import java.util.Map;


public interface LoanApplicationService {

    LoanApplicationResponse submit(Long currentUserId, Role role, LoanApplicationRequest req);

    List<LoanApplicationResponse> listForRole(Long currentUserId, Role role, Long branchId);

    AssessmentResponse getAssessment(Long applicationId);

    Map<String, Object> decide(Long creditOfficerId, Long applicationId, DecisionRequest req);
}
