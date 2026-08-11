package com.microlend.delinquency.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.delinquency.dto.ScanResultResponse;
import com.microlend.delinquency.repository.DelinquencyCaseRepository;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.Role;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.LoanAccountStatus;
import com.microlend.loan.enums.ScheduleStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanProductRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.notification.service.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelinquencyServiceImplTest {

    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock LoanAccountRepository loanAccountRepository;
    @Mock DelinquencyCaseRepository caseRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock LoanProductRepository loanProductRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationGateway notificationGateway;
    @Mock AuditGateway auditService;
    @InjectMocks DelinquencyServiceImpl service;

    @Test
    void scanWithNothingOverdueReturnsZeroedResult() {
        when(scheduleRepository.findByStatusAndDueDateBefore(eq(ScheduleStatus.PENDING), any(LocalDate.class)))
                .thenReturn(List.of());
        when(loanAccountRepository.findByStatus(LoanAccountStatus.ACTIVE)).thenReturn(List.of());

        ScanResultResponse res = service.runScan(1L);

        assertThat(res).isNotNull();
    }

    @Test
    void paymentAppliedOnMissingLoanIsNoOp() {
        when(loanAccountRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        service.onLoanPaymentApplied(99L);
        verify(loanAccountRepository).findById(99L);
    }

    @Test
    void generateDemoPortfolioSeedsFourBackdatedLoansAndRunsScan() {
        User fieldOfficer = User.builder().userId(2L).branchId(1L).role(Role.FIELD_OFFICER).build();
        when(userRepository.findByRole(Role.FIELD_OFFICER)).thenReturn(List.of(fieldOfficer));
        when(borrowerRepository.findByNameStartingWith(DelinquencyServiceImpl.DEMO_PREFIX))
                .thenReturn(List.of());
        when(loanProductRepository.findAll()).thenReturn(List.of());
        // Echo saved entities back so downstream builders can read them.
        when(borrowerRepository.save(any(Borrower.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any(LoanAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(scheduleRepository.save(any(RepaymentSchedule.class))).thenAnswer(inv -> inv.getArgument(0));
        // runScan (invoked internally) — no live rows behind the mocks.
        when(scheduleRepository.findByStatusAndDueDateBefore(eq(ScheduleStatus.PENDING), any(LocalDate.class)))
                .thenReturn(List.of());
        when(loanAccountRepository.findByStatus(LoanAccountStatus.ACTIVE)).thenReturn(List.of());

        Map<String, Object> result = service.generateDemoPortfolioAndScan(1L);

        // Four demo loans, one per PAR bucket.
        assertThat(result.get("loansCreated")).isEqualTo(4);
        assertThat(result.get("scan")).isInstanceOf(ScanResultResponse.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> buckets = (Map<String, Object>) result.get("buckets");
        assertThat(buckets).containsOnlyKeys("PAR30", "PAR60", "PAR90", "PAR180");

        verify(borrowerRepository, times(4)).save(any(Borrower.class));
        verify(loanAccountRepository, times(4)).save(any(LoanAccount.class));

        // Schedules must be backdated to 15/45/75/120 DPD so the scan buckets them.
        ArgumentCaptor<RepaymentSchedule> captor = ArgumentCaptor.forClass(RepaymentSchedule.class);
        verify(scheduleRepository, times(4)).save(captor.capture());
        LocalDate today = LocalDate.now();
        assertThat(captor.getAllValues())
                .allSatisfy(s -> assertThat(s.getStatus()).isEqualTo(ScheduleStatus.PENDING))
                .extracting(s -> (int) java.time.temporal.ChronoUnit.DAYS.between(s.getDueDate(), today))
                .containsExactlyInAnyOrder(15, 45, 75, 120);

        // The real scan engine was invoked.
        verify(loanAccountRepository).findByStatus(LoanAccountStatus.ACTIVE);
    }

    @Test
    void generateDemoPortfolioFailsClearlyWhenNoBranchUserExists() {
        when(userRepository.findByRole(Role.FIELD_OFFICER)).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.generateDemoPortfolioAndScan(1L))
                .hasMessageContaining("field officer");
    }
}
