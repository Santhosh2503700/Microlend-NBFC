package com.microlend.loan.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.borrower.repository.CreditAssessmentRepository;
import com.microlend.borrower.service.CreditAssessmentService;
import com.microlend.common.ApiException;
import com.microlend.identity.enums.Role;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.dto.LoanApplicationRequest;
import com.microlend.loan.repository.LoanApplicationRepository;
import com.microlend.loan.repository.LoanProductRepository;
import com.microlend.loan.repository.SanctionLetterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @Mock LoanApplicationRepository applicationRepository;
    @Mock LoanProductRepository productRepository;
    @Mock SanctionLetterRepository sanctionLetterRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock BorrowerKYCRepository kycRepository;
    @Mock CreditAssessmentRepository assessmentRepository;
    @Mock CreditAssessmentService assessmentService;
    @Mock EmiCalculationService emiService;
    @Mock UserRepository userRepository;
    @Mock AuditGateway auditService;
    @InjectMocks LoanApplicationServiceImpl service;

    private LoanApplicationRequest req() {
        return new LoanApplicationRequest(9L, 1L, new BigDecimal("50000"), "purpose", null);
    }

    @Test
    void submitThrowsWhenBorrowerMissing() {
        when(borrowerRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.submit(1L, Role.FIELD_OFFICER, req()))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void submitForbiddenWhenFieldOfficerDoesNotOwnBorrower() {
        Borrower b = Borrower.builder().borrowerId(9L).registeredByFieldOfficerId(2L).build();
        when(borrowerRepository.findById(9L)).thenReturn(Optional.of(b));
        assertThatThrownBy(() -> service.submit(999L, Role.FIELD_OFFICER, req()))
                .isInstanceOf(ApiException.class);
    }
}
