package com.microlend.loan.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.loan.entity.LoanApplication;
import com.microlend.loan.entity.SanctionLetter;
import com.microlend.loan.enums.SanctionStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanApplicationRepository;
import com.microlend.loan.repository.LoanProductRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.loan.repository.SanctionLetterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanctionAcceptanceServiceImplTest {

    @Mock SanctionLetterRepository sanctionLetterRepository;
    @Mock LoanApplicationRepository applicationRepository;
    @Mock LoanAccountRepository loanAccountRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock LoanProductRepository productRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock EmiCalculationService emiService;
    @Mock AuditGateway auditService;
    @InjectMocks SanctionAcceptanceServiceImpl service;

    private void ownedLetter(SanctionStatus status, Long ownerPortalUserId) {
        SanctionLetter letter = SanctionLetter.builder().sanctionId(1L).applicationId(5L).status(status).build();
        LoanApplication app = LoanApplication.builder().applicationId(5L).borrowerId(7L).build();
        Borrower b = Borrower.builder().borrowerId(7L).portalUserId(ownerPortalUserId).build();
        when(sanctionLetterRepository.findById(1L)).thenReturn(Optional.of(letter));
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));
        when(borrowerRepository.findById(7L)).thenReturn(Optional.of(b));
    }

    @Test
    void acceptRejectedWhenLetterNotIssued() {
        ownedLetter(SanctionStatus.ACCEPTED, 100L);
        assertThatThrownBy(() -> service.accept(100L, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectForbiddenForNonOwner() {
        ownedLetter(SanctionStatus.ISSUED, 100L);
        assertThatThrownBy(() -> service.reject(999L, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void acceptNotFoundWhenMissing() {
        when(sanctionLetterRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.accept(100L, 1L)).isInstanceOf(ApiException.class);
    }
}
