package com.geriatriccare.repository;

import com.geriatriccare.entity.MedicationAdherenceReport;
import com.geriatriccare.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationAdherenceReportRepository extends JpaRepository<MedicationAdherenceReport, UUID> {

    List<MedicationAdherenceReport> findByPatientIdOrderByGeneratedAtDesc(UUID patientId);

    Page<MedicationAdherenceReport> findByPatientId(UUID patientId, Pageable pageable);

    List<MedicationAdherenceReport> findByReportTypeAndPatientId(ReportType reportType, UUID patientId);

    List<MedicationAdherenceReport> findByPatientIdAndMedicationId(UUID patientId, UUID medicationId);

    @Query("SELECT r FROM MedicationAdherenceReport r WHERE r.patientId = :patientId " +
           "AND r.generatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.generatedAt DESC")
    List<MedicationAdherenceReport> findByPatientAndDateRange(
        @Param("patientId") UUID patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM MedicationAdherenceReport r WHERE r.expiresAt < :now")
    List<MedicationAdherenceReport> findExpiredReports(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM MedicationAdherenceReport r WHERE r.patientId = :patientId")
    long countByPatientId(@Param("patientId") UUID patientId);

    List<MedicationAdherenceReport> findByIsHighRiskTrue();

    void deleteByExpiresAtBefore(LocalDateTime date);
}
