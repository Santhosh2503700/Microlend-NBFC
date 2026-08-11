package com.microlend.grouporigination.repository;

import com.microlend.grouporigination.entity.CentreMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentreMeetingRepository extends JpaRepository<CentreMeeting, Long> {

    List<CentreMeeting> findByCentreId(Long centreId);

    List<CentreMeeting> findByConductedById(Long conductedById);
}
