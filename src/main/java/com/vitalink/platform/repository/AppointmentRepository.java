package com.vitalink.platform.repository;

import com.vitalink.platform.entity.Appointment;
import com.vitalink.platform.entity.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    Page<Appointment> findByProfessionalId(UUID professionalId, Pageable pageable);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.professional.id = :professionalId "
            + "AND a.status IN :blockingStatuses "
            + "AND a.scheduledStart < :end "
            + "AND a.scheduledEnd > :start")
    List<Appointment> findConflicts(
            @Param("professionalId") UUID professionalId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("blockingStatuses") Collection<AppointmentStatus> blockingStatuses);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.professional.id = :professionalId "
            + "AND a.status IN :blockingStatuses "
            + "AND a.id <> :excludedId "
            + "AND a.scheduledStart < :end "
            + "AND a.scheduledEnd > :start")
    List<Appointment> findConflictsExcluding(
            @Param("professionalId") UUID professionalId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("blockingStatuses") Collection<AppointmentStatus> blockingStatuses,
            @Param("excludedId") UUID excludedId);
}
