package com.microlend.grouporigination.service;

import com.microlend.grouporigination.dto.CentreRequest;
import com.microlend.grouporigination.dto.CentreResponse;
import com.microlend.grouporigination.entity.Centre;

import java.util.List;


public interface CentreService {

    CentreResponse create(Long officerId, CentreRequest req);

    List<CentreResponse> listForOfficer(Long officerId);

    CentreResponse update(Long officerId, Long centreId, CentreRequest req);

    void delete(Long officerId, Long centreId);

    Centre getOwnedCentre(Long officerId, Long centreId);
}
