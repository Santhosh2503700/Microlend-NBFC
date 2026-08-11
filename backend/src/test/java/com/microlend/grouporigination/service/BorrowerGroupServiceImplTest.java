package com.microlend.grouporigination.service;

import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.enums.BorrowerType;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.grouporigination.dto.GroupRequest;
import com.microlend.grouporigination.dto.GroupResponse;
import com.microlend.grouporigination.entity.BorrowerGroup;
import com.microlend.grouporigination.entity.Centre;
import com.microlend.grouporigination.repository.BorrowerGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerGroupServiceImplTest {

    @Mock
    BorrowerGroupRepository groupRepository;
    @Mock
    BorrowerRepository borrowerRepository;
    @Mock
    CentreService centreService;
    @InjectMocks
    BorrowerGroupServiceImpl service;

    @Test
    void createGroupAttachesMembersAndComputesCount() {
        Centre centre = Centre.builder().centreId(3L).build();
        when(centreService.getOwnedCentre(2L, 3L)).thenReturn(centre);
        when(groupRepository.save(any(BorrowerGroup.class))).thenAnswer(inv -> {
            BorrowerGroup g = inv.getArgument(0);
            if (g.getGroupId() == null) {
                g.setGroupId(10L);
            }
            return g;
        });
        Borrower b1 = Borrower.builder().borrowerId(1L).registeredByFieldOfficerId(2L).build();
        Borrower b2 = Borrower.builder().borrowerId(2L).registeredByFieldOfficerId(2L).build();
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(b1));
        when(borrowerRepository.findById(2L)).thenReturn(Optional.of(b2));
        when(borrowerRepository.save(any(Borrower.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupResponse res = service.create(2L, new GroupRequest("JLG-1", 3L, true, List.of(1L, 2L)));

        assertThat(res.memberCount()).isEqualTo(2);
        assertThat(b1.getGroupId()).isEqualTo(10L);
        assertThat(b1.getBorrowerType()).isEqualTo(BorrowerType.GROUP);
        assertThat(b2.getGroupId()).isEqualTo(10L);
    }

    @Test
    void createGroupRejectsBorrowerFromAnotherOfficer() {
        Centre centre = Centre.builder().centreId(3L).build();
        when(centreService.getOwnedCentre(2L, 3L)).thenReturn(centre);
        when(groupRepository.save(any(BorrowerGroup.class))).thenAnswer(inv -> {
            BorrowerGroup g = inv.getArgument(0);
            if (g.getGroupId() == null) {
                g.setGroupId(10L);
            }
            return g;
        });
        Borrower foreign = Borrower.builder().borrowerId(1L).registeredByFieldOfficerId(999L).build();
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.create(2L, new GroupRequest("JLG-1", 3L, true, List.of(1L))))
                .isInstanceOf(ApiException.class);
    }
}
