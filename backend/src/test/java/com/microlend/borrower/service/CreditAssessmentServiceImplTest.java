package com.microlend.borrower.service;

import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.borrower.repository.CreditAssessmentRepository;
import com.microlend.common.ApiException;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.loan.repository.SanctionLetterRepository;
import com.microlend.loan.service.EmiCalculationService;
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
class CreditAssessmentServiceImplTest {

    @Mock CreditPolicyProperties policy;
    @Mock EmiCalculationService emiService;
    @Mock BorrowerRepository borrowerRepository;
    @Mock BorrowerKYCRepository kycRepository;
    @Mock LoanAccountRepository loanAccountRepository;
    @Mock SanctionLetterRepository sanctionLetterRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock CreditAssessmentRepository assessmentRepository;
    @InjectMocks CreditAssessmentServiceImpl service;

    @Test
    void assessThrowsWhenBorrowerMissing() {
        when(borrowerRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.assess(1L, 7L, LoanProduct.builder().build(), new BigDecimal("50000")))
                .isInstanceOf(ApiException.class);
    }
}
