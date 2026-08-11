package com.microlend.collection.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.collection.dto.CollectionRequest;
import com.microlend.collection.enums.CollectionMode;
import com.microlend.collection.repository.CollectionReceiptRepository;
import com.microlend.collection.repository.CollectionRecordRepository;
import com.microlend.common.ApiException;
import com.microlend.delinquency.service.DelinquencyService;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.notification.service.NotificationGateway;
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
class CollectionServiceImplTest {

    @Mock CollectionRecordRepository recordRepository;
    @Mock CollectionReceiptRepository receiptRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock LoanAccountRepository loanAccountRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock UserRepository userRepository;
    @Mock AuditGateway auditService;
    @Mock NotificationGateway notificationGateway;
    @Mock DelinquencyService delinquencyService;
    @InjectMocks CollectionServiceImpl service;

    private CollectionRequest req() {
        return new CollectionRequest(1L, 5L, new BigDecimal("100"), CollectionMode.CASH, null);
    }

    @Test
    void recordThrowsWhenLoanAccountMissing() {
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.record(2L, req())).isInstanceOf(ApiException.class);
    }

    @Test
    void recordForbiddenWhenOfficerNotOwner() {
        LoanAccount acc = LoanAccount.builder().loanAccountId(1L).borrowerId(7L).build();
        Borrower b = Borrower.builder().borrowerId(7L).registeredByFieldOfficerId(2L).build();
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(borrowerRepository.findById(7L)).thenReturn(Optional.of(b));
        assertThatThrownBy(() -> service.record(999L, req())).isInstanceOf(ApiException.class);
    }
}
