package com.microlend.grouporigination.service;

import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.grouporigination.dto.CentreRequest;
import com.microlend.grouporigination.dto.CentreResponse;
import com.microlend.grouporigination.entity.Centre;
import com.microlend.grouporigination.repository.BorrowerGroupRepository;
import com.microlend.grouporigination.repository.CentreRepository;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CentreServiceImplTest {

    @Mock CentreRepository centreRepository;
    @Mock UserRepository userRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock BorrowerGroupRepository groupRepository;
    @InjectMocks CentreServiceImpl service;

    private CentreRequest req() {
        return new CentreRequest("Womens Centre", "Ananthapur", "MONDAY", LocalTime.of(10, 0));
    }

    @Test
    void createStampsOfficerBranch() {
        User officer = User.builder().userId(2L).branchId(5L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(officer));
        when(centreRepository.save(any(Centre.class))).thenAnswer(inv -> inv.getArgument(0));
        CentreResponse res = service.create(2L, req());
        assertThat(res.branchId()).isEqualTo(5L);
        assertThat(res.createdByFieldOfficerId()).isEqualTo(2L);
    }

    @Test
    void getOwnedCentreForbidsAnotherOfficer() {
        Centre c = Centre.builder().centreId(1L).createdByFieldOfficerId(2L).build();
        when(centreRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.getOwnedCentre(99L, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void updateChangesFieldsForOwner() {
        Centre c = Centre.builder().centreId(1L).createdByFieldOfficerId(2L).centreName("Old").build();
        when(centreRepository.findById(1L)).thenReturn(Optional.of(c));
        when(centreRepository.save(any(Centre.class))).thenAnswer(inv -> inv.getArgument(0));
        CentreResponse res = service.update(2L, 1L, req());
        assertThat(res.centreName()).isEqualTo("Womens Centre");
    }

    @Test
    void deleteRefusedWhenBorrowersStillReferenceCentre() {
        Centre c = Centre.builder().centreId(1L).createdByFieldOfficerId(2L).build();
        when(centreRepository.findById(1L)).thenReturn(Optional.of(c));
        when(borrowerRepository.findByCentreId(1L)).thenReturn(List.of(Borrower.builder().borrowerId(7L).build()));
        assertThatThrownBy(() -> service.delete(2L, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void deleteSucceedsWhenNoReferences() {
        Centre c = Centre.builder().centreId(1L).createdByFieldOfficerId(2L).build();
        when(centreRepository.findById(1L)).thenReturn(Optional.of(c));
        when(borrowerRepository.findByCentreId(1L)).thenReturn(List.of());
        when(groupRepository.findByCentreId(1L)).thenReturn(List.of());
        service.delete(2L, 1L);
        verify(centreRepository).delete(c);
    }
}
