package com.microlend.identity.service;

import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.delinquency.repository.DelinquencyCaseRepository;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.Role;
import com.microlend.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDirectoryServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    BorrowerRepository borrowerRepository;
    @Mock
    DelinquencyCaseRepository delinquencyCaseRepository;
    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @InjectMocks
    UserDirectoryServiceImpl service;

    @Test
    void listAllUsersMapsAndSortsById() {
        User a = User.builder().userId(2L).name("B").email("b@x.com").role(Role.FIELD_OFFICER).branchId(1L).build();
        User b = User.builder().userId(1L).name("A").email("a@x.com").role(Role.NBFC_ADMIN).branchId(1L).build();
        when(userRepository.findAll()).thenReturn(List.of(a, b));

        var res = service.listAllUsers();

        assertThat(res).hasSize(2);
        assertThat(res.get(0).userId()).isEqualTo(1L); // sorted ascending
    }

    @Test
    void deleteUserRejectsMissing() {
        when(userRepository.existsById(9L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteUser(9L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteUserRemovesExisting() {
        when(userRepository.existsById(3L)).thenReturn(true);
        service.deleteUser(3L);
        verify(userRepository).deleteById(3L);
    }
}
