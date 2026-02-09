package com.geriatriccare.service;

import com.geriatriccare.dto.report.AdherenceReportRequest;
import com.geriatriccare.dto.report.AdherenceReportResponse;
import com.geriatriccare.dto.report.AdherenceStatistics;
import com.geriatriccare.entity.AdherenceReport;
import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.enums.ReportType;
import com.geriatriccare.enums.TimePeriod;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.AdherenceReportRepository;
import com.geriatriccare.repository.CarePlanRepository;
import com.geriatriccare.repository.CareTaskRepository;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdherenceReportService Unit Tests")
class AdherenceReportServiceTest {

    @Mock
    private AdherenceReportRepository reportRepository;
    @Mock
    private CareTaskRepository careTaskRepository;
    @Mock
    private CarePlanRepository carePlanRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AdherenceReportService reportService;

    private UUID patientId;
    private UUID userId;
    private Patient patient;
    private AdherenceReport report;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        userId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1950, 1, 1));

        report = new AdherenceReport();
        report.setId(UUID.randomUUID());
        report.setPatientId(patientId);
        report.setReportType(ReportType.CARE_PLAN_ADHERENCE);
        report.setTotalTasks(10);
        report.setCompletedTasks(7);
        report.setMissedTasks(3);
        report.setAdherencePercentage(70.0);
    }

    @Test
    @DisplayName("Should generate report successfully")
    void generateReport_Success() {
        // Given
        AdherenceReportRequest request = AdherenceReportRequest.builder()
            .patientId(patientId)
            .reportType(ReportType.CARE_PLAN_ADHERENCE)
            .timePeriod(TimePeriod.LAST_30_DAYS)
            .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(carePlanRepository.findByPatientIdAndIsActiveTrue(any(), any()))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(reportRepository.save(any(AdherenceReport.class))).thenReturn(report);

        // When
        AdherenceReportResponse response = reportService.generateReport(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPatientId()).isEqualTo(patientId);
        verify(reportRepository).save(any(AdherenceReport.class));
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void generateReport_PatientNotFound() {
        // Given
        AdherenceReportRequest request = AdherenceReportRequest.builder()
            .patientId(patientId)
            .reportType(ReportType.CARE_PLAN_ADHERENCE)
            .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> reportService.generateReport(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Patient not found");
    }

    @Test
    @DisplayName("Should get report by ID")
    void getReport_Success() {
        // Given
        when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When
        AdherenceReportResponse response = reportService.getReport(report.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportId()).isEqualTo(report.getId());
    }

    @Test
    @DisplayName("Should get adherence statistics")
    void getAdherenceStatistics_Success() {
        // Given
        when(carePlanRepository.findByPatientIdAndIsActiveTrue(any(), any()))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        AdherenceStatistics stats = reportService.getAdherenceStatistics(patientId, 30);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getPatientId()).isEqualTo(patientId);
    }
}
