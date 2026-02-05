package com.geriatriccare.service;

import com.geriatriccare.dto.alert.AlertResponse;
import com.geriatriccare.entity.Alert;
import com.geriatriccare.entity.AlertRule;
import com.geriatriccare.entity.VitalSign;
import com.geriatriccare.enums.AlertStatus;
import com.geriatriccare.enums.VitalSignType;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.AlertRepository;
import com.geriatriccare.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertRuleService alertRuleService;
    private final SecurityUtil securityUtil;

    @Transactional
    public List<Alert> evaluateVitalSign(VitalSign vitalSign) {
        log.info("Evaluating vital sign {} for alerts", vitalSign.getId());
        
        List<Alert> triggeredAlerts = new java.util.ArrayList<>();

        // Check Blood Pressure
        if (vitalSign.getBloodPressureSystolic() != null) {
            triggeredAlerts.addAll(checkValue(
                vitalSign, 
                VitalSignType.BLOOD_PRESSURE, 
                vitalSign.getBloodPressureSystolic().doubleValue()
            ));
        }

        // Check Heart Rate
        if (vitalSign.getHeartRate() != null) {
            triggeredAlerts.addAll(checkValue(
                vitalSign, 
                VitalSignType.HEART_RATE, 
                vitalSign.getHeartRate().doubleValue()
            ));
        }

        // Check Temperature
        if (vitalSign.getTemperature() != null) {
            triggeredAlerts.addAll(checkValue(
                vitalSign, 
                VitalSignType.TEMPERATURE, 
                vitalSign.getTemperature()
            ));
        }

        // Check Respiratory Rate
        if (vitalSign.getRespiratoryRate() != null) {
            triggeredAlerts.addAll(checkValue(
                vitalSign, 
                VitalSignType.RESPIRATORY_RATE, 
                vitalSign.getRespiratoryRate().doubleValue()
            ));
        }

        // Check Oxygen Saturation
        if (vitalSign.getOxygenSaturation() != null) {
            triggeredAlerts.addAll(checkValue(
                vitalSign, 
                VitalSignType.OXYGEN_SATURATION, 
                vitalSign.getOxygenSaturation().doubleValue()
            ));
        }

        log.info("Triggered {} alerts for vital sign {}", triggeredAlerts.size(), vitalSign.getId());
        return triggeredAlerts;
    }

    private List<Alert> checkValue(VitalSign vitalSign, VitalSignType type, Double value) {
        List<AlertRule> rules = alertRuleService.getApplicableRules(vitalSign.getPatientId(), type);
        List<Alert> alerts = new java.util.ArrayList<>();

        for (AlertRule rule : rules) {
            if (shouldTriggerAlert(rule, value) && !hasRecentAlert(vitalSign, rule)) {
                Alert alert = createAlert(vitalSign, rule, value);
                alerts.add(alert);
            }
        }

        return alerts;
    }

    private boolean shouldTriggerAlert(AlertRule rule, Double value) {
        switch (rule.getComparisonOperator()) {
            case GREATER_THAN:
                return value > rule.getThresholdValue();
            case LESS_THAN:
                return value < rule.getThresholdValue();
            case BETWEEN:
                return value >= rule.getThresholdValue() && 
                       value <= rule.getThresholdValueMax();
            case EQUALS:
                return Math.abs(value - rule.getThresholdValue()) < 0.01;
            default:
                return false;
        }
    }

    private boolean hasRecentAlert(VitalSign vitalSign, AlertRule rule) {
        LocalDateTime cooldownStart = LocalDateTime.now().minusMinutes(rule.getCooldownMinutes());
        List<Alert> recentAlerts = alertRepository.findRecentSimilarAlerts(
            vitalSign.getId(), 
            rule.getId(), 
            cooldownStart
        );
        return !recentAlerts.isEmpty();
    }

    private Alert createAlert(VitalSign vitalSign, AlertRule rule, Double value) {
        Alert alert = new Alert();
        alert.setPatientId(vitalSign.getPatientId());
        alert.setVitalSignId(vitalSign.getId());
        alert.setTriggeredRuleId(rule.getId());
        alert.setSeverity(rule.getSeverity());
        alert.setMessage(String.format(rule.getAlertMessage(), value));
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setStatus(AlertStatus.NEW);

        alert = alertRepository.save(alert);
        log.info("Alert created: {} - {}", alert.getId(), alert.getMessage());
        
        return alert;
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlert(UUID id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        return convertToResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlerts(UUID patientId) {
        return alertRepository.findByPatientIdAndStatusOrderByTriggeredAtDesc(patientId, AlertStatus.NEW)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByPatient(UUID patientId) {
        return alertRepository.findByPatientIdAndStatuses(
            patientId, 
            Arrays.asList(AlertStatus.NEW, AlertStatus.ACKNOWLEDGED)
        ).stream()
          .map(this::convertToResponse)
          .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlertsByPatientPaginated(UUID patientId, Pageable pageable) {
        return alertRepository.findByPatientIdOrderByTriggeredAtDesc(patientId, pageable)
            .map(this::convertToResponse);
    }

    @Transactional
    public AlertResponse acknowledgeAlert(UUID id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(securityUtil.getCurrentUserId());

        alert = alertRepository.save(alert);
        log.info("Alert acknowledged: {}", id);

        return convertToResponse(alert);
    }

    @Transactional
    public AlertResponse resolveAlert(UUID id, String notes) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(securityUtil.getCurrentUserId());
        alert.setNotes(notes);

        alert = alertRepository.save(alert);
        log.info("Alert resolved: {}", id);

        return convertToResponse(alert);
    }

    @Transactional(readOnly = true)
    public long getActiveAlertCount(UUID patientId) {
        return alertRepository.countByPatientIdAndStatus(patientId, AlertStatus.NEW);
    }

    private AlertResponse convertToResponse(Alert alert) {
        return AlertResponse.builder()
            .id(alert.getId())
            .patientId(alert.getPatientId())
            .vitalSignId(alert.getVitalSignId())
            .triggeredRuleId(alert.getTriggeredRuleId())
            .severity(alert.getSeverity())
            .message(alert.getMessage())
            .triggeredAt(alert.getTriggeredAt())
            .acknowledgedAt(alert.getAcknowledgedAt())
            .acknowledgedBy(alert.getAcknowledgedBy())
            .resolvedAt(alert.getResolvedAt())
            .resolvedBy(alert.getResolvedBy())
            .status(alert.getStatus())
            .notes(alert.getNotes())
            .createdAt(alert.getCreatedAt())
            .build();
    }
}
