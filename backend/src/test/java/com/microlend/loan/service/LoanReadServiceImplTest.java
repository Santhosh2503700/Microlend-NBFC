package com.microlend.loan.service;

import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.borrower.service.BorrowerService;
import com.microlend.common.ApiException;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanApplicationRepository;
import com.microlend.loan.repository.LoanProductRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanReadServiceImplTest {

    @Mock LoanAccountRepository loanAccountRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock LoanApplicationRepository applicationRepository;
    @Mock LoanProductRepository productRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock BorrowerService borrowerService;
    @InjectMocks LoanReadServiceImpl service;

    @Test
    void requireBorrowerThrowsWhenNoProfileLinked() {
        when(borrowerRepository.findByPortalUserId(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.requireBorrowerForPortalUser(100L)).isInstanceOf(ApiException.class);
    }

    @Test
    void dashboardThrowsWhenNoProfileLinked() {
        when(borrowerRepository.findByPortalUserId(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.dashboardForPortalUser(100L)).isInstanceOf(ApiException.class);
    }
}
