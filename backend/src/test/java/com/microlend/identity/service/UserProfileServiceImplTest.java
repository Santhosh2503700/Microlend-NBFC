package com.microlend.identity.service;

import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.identity.dto.UserProfileResponse;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.Role;
import com.microlend.identity.enums.UserStatus;
import com.microlend.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock BorrowerRepository borrowerRepository;
    @InjectMocks UserProfileServiceImpl service;

    @Test
    void getProfileThrowsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getProfile(1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void getProfileReturnsBasicsForNonBorrower() {
        User u = User.builder().userId(1L).name("Admin").email("a@b.com").phone("9990001111")
                .role(Role.NBFC_ADMIN).status(UserStatus.ACTIVE).branchId(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserProfileResponse res = service.getProfile(1L);

        assertThat(res.getName()).isEqualTo("Admin");
        assertThat(res.getRole()).isEqualTo(Role.NBFC_ADMIN);
    }
}
