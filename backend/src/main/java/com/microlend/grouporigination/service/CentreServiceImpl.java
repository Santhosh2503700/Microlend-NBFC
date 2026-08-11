package com.microlend.grouporigination.service;

import com.microlend.common.ApiException;
import com.microlend.grouporigination.dto.CentreRequest;
import com.microlend.grouporigination.dto.CentreResponse;
import com.microlend.grouporigination.entity.Centre;
import com.microlend.grouporigination.enums.CommonStatus;
import com.microlend.grouporigination.repository.BorrowerGroupRepository;
import com.microlend.grouporigination.repository.CentreRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentreServiceImpl implements CentreService {

    private final CentreRepository centreRepository;
    private final UserRepository userRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowerGroupRepository groupRepository;

    @Override
    @Transactional
    public CentreResponse create(Long officerId, CentreRequest req) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> ApiException.unauthorized("Officer not found"));
        Centre c = Centre.builder()
                .centreName(req.centreName())
                .village(req.village())
                .meetingDay(req.meetingDay())
                .meetingTime(req.meetingTime())
                .branchId(officer.getBranchId())
                .createdByFieldOfficerId(officerId)
                .status(CommonStatus.ACTIVE)
                .build();
        return CentreResponse.from(centreRepository.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentreResponse> listForOfficer(Long officerId) {
        return centreRepository.findByCreatedByFieldOfficerId(officerId).stream()
                .map(CentreResponse::from).toList();
    }

    @Override
    @Transactional
    public CentreResponse update(Long officerId, Long centreId, CentreRequest req) {
        Centre c = getOwnedCentre(officerId, centreId);
        c.setCentreName(req.centreName());
        c.setVillage(req.village());
        c.setMeetingDay(req.meetingDay());
        c.setMeetingTime(req.meetingTime());
        return CentreResponse.from(centreRepository.save(c));
    }

    @Override
    @Transactional
    public void delete(Long officerId, Long centreId) {
        Centre c = getOwnedCentre(officerId, centreId);
        if (!borrowerRepository.findByCentreId(centreId).isEmpty()) {
            throw ApiException.badRequest("Cannot delete a centre that still has borrowers assigned to it");
        }
        if (!groupRepository.findByCentreId(centreId).isEmpty()) {
            throw ApiException.badRequest("Cannot delete a centre that still has groups under it");
        }
        centreRepository.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Centre getOwnedCentre(Long officerId, Long centreId) {
        Centre c = centreRepository.findById(centreId)
                .orElseThrow(() -> ApiException.notFound("Centre not found: " + centreId));
        if (!c.getCreatedByFieldOfficerId().equals(officerId)) {
            throw ApiException.forbidden("Centre does not belong to this officer");
        }
        return c;
    }
}
