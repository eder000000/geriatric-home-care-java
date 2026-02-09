package com.geriatriccare.service;

import com.geriatriccare.dto.dashboard.DashboardRequest;
import com.geriatriccare.dto.dashboard.DashboardStatistics;
import com.geriatriccare.entity.*;
import com.geriatriccare.enums.TimePeriod;
import com.geriatriccare.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private VitalSignRepository vitalSignRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private AlertRuleRepository alertRuleRepository;
    @Mock
    private AdherenceReportRepository adherenceReportRepository;
    @Mock
    private MedicationAdherenceReportRepository medicationReportRepository;
    @Mock
    private CarePlanRepository carePlanRepository;
    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should generate dashboard statistics successfully")
    void generateDashboard_Success() {
        // Given
        DashboardRequest request = DashboardRequest.builder()
            .timePeriod(TimePeriod.LAST_30_DAYS)
            .build();

        when(patientRepository.count()).thenReturn(100L);
        when(patientRepository.findByIsActiveTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<Patient>emptyList()));
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());
        when(vitalSignRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<VitalSign>emptyList()));
        when(alertRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<Alert>emptyList()));
        when(adherenceReportRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<AdherenceReport>emptyList()));
        when(medicationReportRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<MedicationAdherenceReport>emptyList()));
        when(carePlanRepository.count()).thenReturn(50L);
        when(carePlanRepository.findByIsActiveTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<CarePlan>emptyList()));
        when(medicationRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        // When
        DashboardStatistics stats = dashboardService.generateDashboard(request);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getGeneratedAt()).isNotNull();
        assertThat(stats.getPopulationStats()).isNotNull();
        assertThat(stats.getVitalSignsMetrics()).isNotNull();
        assertThat(stats.getAlertMetrics()).isNotNull();
        assertThat(stats.getCarePlanMetrics()).isNotNull();
        assertThat(stats.getMedicationMetrics()).isNotNull();
    }

    @Test
    @DisplayName("Should handle default request")
    void generateDashboard_DefaultRequest() {
        // Given
        DashboardRequest request = new DashboardRequest();
        
        when(patientRepository.count()).thenReturn(50L);
        when(patientRepository.findByIsActiveTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<Patient>emptyList()));
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());
        when(vitalSignRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<VitalSign>emptyList()));
        when(alertRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<Alert>emptyList()));
        when(adherenceReportRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<AdherenceReport>emptyList()));
        when(medicationReportRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<MedicationAdherenceReport>emptyList()));
        when(carePlanRepository.count()).thenReturn(25L);
        when(carePlanRepository.findByIsActiveTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<CarePlan>emptyList()));
        when(medicationRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        // When
        DashboardStatistics stats = dashboardService.generateDashboard(request);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getPopulationStats().getTotalPatients()).isEqualTo(50);
    }
}
