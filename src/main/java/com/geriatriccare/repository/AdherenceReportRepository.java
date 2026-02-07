package com.geriatriccare.repository;

import com.geriatriccare.entity.AdherenceReport;
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
public interface AdherenceReportRepository extends JpaRepository<AdherenceReport, UUID> {

    List<AdherenceReport> findByPatientIdOrderByGeneratedAtDesc(UUID patientId);

    Page<AdherenceReport> findByPatientId(UUID patientId, Pageable pageable);

    List<AdherenceReport> findByReportTypeAndPatientId(ReportType reportType, UUID patientId);

    @Query("SELECT r FROM AdherenceReport r WHERE r.patientId = :patientId " +
           "AND r.generatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.generatedAt DESC")
    List<AdherenceReport> findByPatientAndDateRange(
        @Param("patientId") UUID patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM AdherenceReport r WHERE r.expiresAt < :now")
    List<AdherenceReport> findExpiredReports(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM AdherenceReport r WHERE r.patientId = :patientId")
    long countByPatientId(@Param("patientId") UUID patientId);

    void deleteByExpiresAtBefore(LocalDateTime date);
}
