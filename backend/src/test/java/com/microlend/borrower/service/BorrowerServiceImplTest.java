package com.microlend.borrower.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.grouporigination.service.BorrowerGroupService;
import com.microlend.grouporigination.service.CentreService;
import com.microlend.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceImplTest {

    @Mock BorrowerRepository borrowerRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CentreService centreService;
    @Mock BorrowerGroupService groupService;
    @Mock AuditGateway auditService;
    @Mock BorrowerKYCRepository borrowerKYCRepository;
    @InjectMocks BorrowerServiceImpl service;

    @Test
    void getOwnedForbidsAnotherOfficer() {
        Borrower b = Borrower.builder().borrowerId(1L).registeredByFieldOfficerId(2L).build();
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(b));
        assertThatThrownBy(() -> service.getOwned(999L, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void getByIdPrivilegedThrowsWhenMissing() {
        when(borrowerRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getByIdPrivileged(9L)).isInstanceOf(ApiException.class);
    }
}
