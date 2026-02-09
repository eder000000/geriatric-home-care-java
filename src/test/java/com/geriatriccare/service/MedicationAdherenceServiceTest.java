package com.geriatriccare.service;

import com.geriatriccare.dto.medication.MedicationAdherenceReportRequest;
import com.geriatriccare.dto.medication.MedicationAdherenceReportResponse;
import com.geriatriccare.dto.medication.MedicationAdherenceStatistics;
import com.geriatriccare.entity.MedicationAdherenceReport;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.enums.ReportType;
import com.geriatriccare.enums.TimePeriod;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.*;
import com.geriatriccare.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationAdherenceService Unit Tests")
class MedicationAdherenceServiceTest {

    @Mock
    private MedicationAdherenceReportRepository reportRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private MedicationAdherenceService adherenceService;

    private UUID patientId;
    private UUID userId;
    private Patient patient;
    private MedicationAdherenceReport report;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        userId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);
        patient.setFirstName("Jane");
        patient.setLastName("Smith");
        patient.setDateOfBirth(LocalDate.of(1945, 5, 15));

        report = new MedicationAdherenceReport();
        report.setId(UUID.randomUUID());
        report.setPatientId(patientId);
        report.setReportType(ReportType.MEDICATION_ADHERENCE);
        report.setTotalScheduledDoses(60);
        report.setTakenDoses(45);
        report.setMissedDoses(15);
        report.setAdherencePercentage(75.0);
    }

    @Test
    @DisplayName("Should generate medication adherence report successfully")
    void generateReport_Success() {
        // Given
        MedicationAdherenceReportRequest request = MedicationAdherenceReportRequest.builder()
            .patientId(patientId)
            .reportType(ReportType.MEDICATION_ADHERENCE)
            .timePeriod(TimePeriod.LAST_30_DAYS)
            .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(medicationRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(reportRepository.save(any(MedicationAdherenceReport.class))).thenReturn(report);

        // When
        MedicationAdherenceReportResponse response = adherenceService.generateReport(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPatientId()).isEqualTo(patientId);
        verify(reportRepository).save(any(MedicationAdherenceReport.class));
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void generateReport_PatientNotFound() {
        // Given
        MedicationAdherenceReportRequest request = MedicationAdherenceReportRequest.builder()
            .patientId(patientId)
            .reportType(ReportType.MEDICATION_ADHERENCE)
            .build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> adherenceService.generateReport(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Patient not found");
    }

    @Test
    @DisplayName("Should get adherence statistics")
    void getAdherenceStatistics_Success() {
        // Given
        when(medicationRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        // When
        MedicationAdherenceStatistics stats = adherenceService.getAdherenceStatistics(patientId, 30);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getPatientId()).isEqualTo(patientId);
    }
}
