package com.geriatriccare.service;

import com.geriatriccare.dto.alert.AlertResponse;
import com.geriatriccare.entity.Alert;
import com.geriatriccare.entity.AlertRule;
import com.geriatriccare.entity.VitalSign;
import com.geriatriccare.enums.*;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.AlertRepository;
import com.geriatriccare.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertRuleService alertRuleService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AlertService alertService;

    private UUID patientId;
    private UUID userId;
    private VitalSign vitalSign;
    private AlertRule criticalRule;
    private AlertRule warningRule;
    private Alert alert;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        userId = UUID.randomUUID();

        vitalSign = new VitalSign();
        vitalSign.setId(UUID.randomUUID());
        vitalSign.setPatientId(patientId);
        vitalSign.setBloodPressureSystolic(190); // Critical high
        vitalSign.setHeartRate(125); // Critical high
        vitalSign.setTemperature(39.5); // Critical high
        vitalSign.setOxygenSaturation(85); // Critical low

        criticalRule = createAlertRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.GREATER_THAN,
            180.0,
            null,
            "Critical BP: %.0f",
            15
        );

        warningRule = createAlertRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            100.0,
            120.0,
            "Warning HR: %.0f",
            30
        );

        alert = new Alert();
        alert.setId(UUID.randomUUID());
        alert.setPatientId(patientId);
        alert.setVitalSignId(vitalSign.getId());
        alert.setStatus(AlertStatus.NEW);
        alert.setSeverity(AlertSeverity.CRITICAL);
    }

    @Test
    @DisplayName("Should trigger critical alert for high BP")
    void evaluateVitalSign_CriticalBP() {
        // Given
        when(alertRuleService.getApplicableRules(eq(patientId), any(VitalSignType.class)))
            .thenReturn(Collections.emptyList());
        when(alertRuleService.getApplicableRules(patientId, VitalSignType.BLOOD_PRESSURE))
            .thenReturn(Arrays.asList(criticalRule));
        when(alertRepository.findRecentSimilarAlerts(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        // When
        List<Alert> alerts = alertService.evaluateVitalSign(vitalSign);

        // Then
        assertThat(alerts).hasSizeGreaterThanOrEqualTo(1);
        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should not trigger alert when value is normal")
    void evaluateVitalSign_NormalValues() {
        // Given
        vitalSign.setBloodPressureSystolic(120); // Normal
        when(alertRuleService.getApplicableRules(eq(patientId), any(VitalSignType.class)))
            .thenReturn(Collections.emptyList());
        when(alertRuleService.getApplicableRules(patientId, VitalSignType.BLOOD_PRESSURE))
            .thenReturn(Arrays.asList(criticalRule));

        // When
        List<Alert> alerts = alertService.evaluateVitalSign(vitalSign);

        // Then
        assertThat(alerts).isEmpty();
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not trigger duplicate alert within cooldown")
    void evaluateVitalSign_CooldownPrevents() {
        // Given
        when(alertRuleService.getApplicableRules(eq(patientId), any(VitalSignType.class)))
            .thenReturn(Collections.emptyList());
        when(alertRuleService.getApplicableRules(patientId, VitalSignType.BLOOD_PRESSURE))
            .thenReturn(Arrays.asList(criticalRule));
        when(alertRepository.findRecentSimilarAlerts(any(), any(), any()))
            .thenReturn(Arrays.asList(alert)); // Recent alert exists

        // When
        List<Alert> alerts = alertService.evaluateVitalSign(vitalSign);

        // Then
        assertThat(alerts).isEmpty();
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should trigger alert for BETWEEN operator")
    void evaluateVitalSign_BetweenOperator() {
        // Given - Only set heart rate, clear other values to avoid triggering other alerts
        VitalSign hrVitalSign = new VitalSign();
        hrVitalSign.setId(UUID.randomUUID());
        hrVitalSign.setPatientId(patientId);
        hrVitalSign.setHeartRate(110); // Within 100-120 range
        
        when(alertRuleService.getApplicableRules(eq(patientId), any(VitalSignType.class)))
            .thenReturn(Collections.emptyList());
        when(alertRuleService.getApplicableRules(patientId, VitalSignType.HEART_RATE))
            .thenReturn(Arrays.asList(warningRule));
        when(alertRepository.findRecentSimilarAlerts(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        // When
        List<Alert> alerts = alertService.evaluateVitalSign(hrVitalSign);

        // Then
        assertThat(alerts).hasSize(1);
    }

    @Test
    @DisplayName("Should trigger multiple alerts for multiple violations")
    void evaluateVitalSign_MultipleAlerts() {
        // Given
        AlertRule hrRule = createAlertRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.GREATER_THAN,
            120.0,
            null,
            "Critical HR",
            15
        );

        when(alertRuleService.getApplicableRules(eq(patientId), any(VitalSignType.class)))
            .thenReturn(Collections.emptyList());
        when(alertRuleService.getApplicableRules(patientId, VitalSignType.BLOOD_PRESSURE))
            .thenReturn(Arrays.asList(criticalRule));
        when(alertRuleService.getApplicableRules(patientId, VitalSignType.HEART_RATE))
            .thenReturn(Arrays.asList(hrRule));
        when(alertRepository.findRecentSimilarAlerts(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        // When
        List<Alert> alerts = alertService.evaluateVitalSign(vitalSign);

        // Then
        assertThat(alerts).hasSizeGreaterThanOrEqualTo(2);
        verify(alertRepository, atLeast(2)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should get alert by ID")
    void getAlert_Success() {
        // Given
        UUID alertId = alert.getId();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        // When
        AlertResponse response = alertService.getAlert(alertId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(alertId);
    }

    @Test
    @DisplayName("Should throw exception when alert not found")
    void getAlert_NotFound() {
        // Given
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> alertService.getAlert(alertId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get active alerts for patient")
    void getActiveAlerts_Success() {
        // Given
        when(alertRepository.findByPatientIdAndStatusOrderByTriggeredAtDesc(
            patientId, AlertStatus.NEW
        )).thenReturn(Arrays.asList(alert));

        // When
        List<AlertResponse> alerts = alertService.getActiveAlerts(patientId);

        // Then
        assertThat(alerts).hasSize(1);
    }

    @Test
    @DisplayName("Should acknowledge alert")
    void acknowledgeAlert_Success() {
        // Given
        UUID alertId = alert.getId();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        // When
        AlertResponse response = alertService.acknowledgeAlert(alertId);

        // Then
        verify(alertRepository).save(argThat(a -> 
            a.getStatus() == AlertStatus.ACKNOWLEDGED &&
            a.getAcknowledgedBy().equals(userId) &&
            a.getAcknowledgedAt() != null
        ));
    }

    @Test
    @DisplayName("Should resolve alert with notes")
    void resolveAlert_Success() {
        // Given
        UUID alertId = alert.getId();
        String notes = "Patient stabilized after medication";
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        // When
        AlertResponse response = alertService.resolveAlert(alertId, notes);

        // Then
        verify(alertRepository).save(argThat(a -> 
            a.getStatus() == AlertStatus.RESOLVED &&
            a.getResolvedBy().equals(userId) &&
            a.getResolvedAt() != null &&
            a.getNotes().equals(notes)
        ));
    }

    @Test
    @DisplayName("Should get active alert count")
    void getActiveAlertCount_Success() {
        // Given
        when(alertRepository.countByPatientIdAndStatus(patientId, AlertStatus.NEW))
            .thenReturn(5L);

        // When
        long count = alertService.getActiveAlertCount(patientId);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    private AlertRule createAlertRule(VitalSignType type, AlertSeverity severity,
                                     ComparisonOperator operator, Double threshold,
                                     Double thresholdMax, String message, int cooldown) {
        AlertRule rule = new AlertRule();
        rule.setId(UUID.randomUUID());
        rule.setVitalSignType(type);
        rule.setSeverity(severity);
        rule.setComparisonOperator(operator);
        rule.setThresholdValue(threshold);
        rule.setThresholdValueMax(thresholdMax);
        rule.setAlertMessage(message);
        rule.setCooldownMinutes(cooldown);
        rule.setIsActive(true);
        return rule;
    }
}
