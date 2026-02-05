package com.geriatriccare.repository;

import com.geriatriccare.entity.VitalSign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, UUID> {

    List<VitalSign> findByPatientIdAndDeletedFalseOrderByMeasuredAtDesc(UUID patientId);

    Page<VitalSign> findByPatientIdAndDeletedFalse(UUID patientId, Pageable pageable);

    @Query("SELECT v FROM VitalSign v WHERE v.patientId = :patientId AND v.measuredAt BETWEEN :start AND :end AND v.deleted = false ORDER BY v.measuredAt DESC")
    List<VitalSign> findByPatientIdAndDateRange(
        @Param("patientId") UUID patientId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT v FROM VitalSign v WHERE v.patientId = :patientId AND v.deleted = false ORDER BY v.measuredAt DESC")
    Optional<VitalSign> findLatestByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT COUNT(v) FROM VitalSign v WHERE v.patientId = :patientId AND v.deleted = false")
    long countByPatientId(@Param("patientId") UUID patientId);
}
