package com.microlend.analytics.service;

import com.microlend.analytics.dto.ParDistributionRow;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.collection.repository.CollectionRecordRepository;
import com.microlend.grouporigination.repository.BorrowerGroupRepository;
import com.microlend.identity.enums.Role;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanApplicationRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock LoanAccountRepository loanAccountRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock CollectionRecordRepository collectionRepository;
    @Mock LoanApplicationRepository applicationRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock BorrowerGroupRepository groupRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AnalyticsServiceImpl service;

    @Test
    void parDistributionSystemScopeReturnsBucketsWithNoData() {
        when(loanAccountRepository.findAll()).thenReturn(List.of());
        List<ParDistributionRow> rows = service.parDistribution(1L, Role.NBFC_ADMIN, "system");
        assertThat(rows).isNotNull();
    }
}
