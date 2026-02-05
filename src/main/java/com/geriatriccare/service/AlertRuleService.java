package com.geriatriccare.service;

import com.geriatriccare.dto.alert.AlertRuleRequest;
import com.geriatriccare.dto.alert.AlertRuleResponse;
import com.geriatriccare.entity.AlertRule;
import com.geriatriccare.enums.VitalSignType;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.AlertRuleRepository;
import com.geriatriccare.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public AlertRuleResponse createAlertRule(AlertRuleRequest request) {
        log.info("Creating alert rule for type: {}", request.getVitalSignType());

        AlertRule rule = new AlertRule();
        rule.setPatientId(request.getPatientId());
        rule.setVitalSignType(request.getVitalSignType());
        rule.setSeverity(request.getSeverity());
        rule.setComparisonOperator(request.getComparisonOperator());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setThresholdValueMax(request.getThresholdValueMax());
        rule.setAlertMessage(request.getAlertMessage());
        rule.setIsActive(true);
        rule.setCooldownMinutes(request.getCooldownMinutes() != null ? request.getCooldownMinutes() : 30);
        rule.setCreatedBy(securityUtil.getCurrentUserId());

        rule = alertRuleRepository.save(rule);
        log.info("Alert rule created: {}", rule.getId());

        return convertToResponse(rule);
    }

    @Transactional(readOnly = true)
    public AlertRuleResponse getAlertRule(UUID id) {
        AlertRule rule = alertRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found"));
        return convertToResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getAllAlertRules() {
        return alertRuleRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getActiveRules() {
        return alertRuleRepository.findByIsActiveTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getRulesForPatient(UUID patientId) {
        return alertRuleRepository.findByPatientIdAndIsActiveTrue(patientId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertRule> getApplicableRules(UUID patientId, VitalSignType type) {
        return alertRuleRepository.findApplicableRules(patientId, type);
    }

    @Transactional
    public AlertRuleResponse updateAlertRule(UUID id, AlertRuleRequest request) {
        AlertRule rule = alertRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found"));

        rule.setVitalSignType(request.getVitalSignType());
        rule.setSeverity(request.getSeverity());
        rule.setComparisonOperator(request.getComparisonOperator());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setThresholdValueMax(request.getThresholdValueMax());
        rule.setAlertMessage(request.getAlertMessage());
        if (request.getCooldownMinutes() != null) {
            rule.setCooldownMinutes(request.getCooldownMinutes());
        }

        rule = alertRuleRepository.save(rule);
        log.info("Alert rule updated: {}", id);

        return convertToResponse(rule);
    }

    @Transactional
    public void deleteAlertRule(UUID id) {
        AlertRule rule = alertRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found"));
        alertRuleRepository.delete(rule);
        log.info("Alert rule deleted: {}", id);
    }

    @Transactional
    public void deactivateAlertRule(UUID id) {
        AlertRule rule = alertRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found"));
        rule.setIsActive(false);
        alertRuleRepository.save(rule);
        log.info("Alert rule deactivated: {}", id);
    }

    private AlertRuleResponse convertToResponse(AlertRule rule) {
        return AlertRuleResponse.builder()
            .id(rule.getId())
            .patientId(rule.getPatientId())
            .vitalSignType(rule.getVitalSignType())
            .severity(rule.getSeverity())
            .comparisonOperator(rule.getComparisonOperator())
            .thresholdValue(rule.getThresholdValue())
            .thresholdValueMax(rule.getThresholdValueMax())
            .alertMessage(rule.getAlertMessage())
            .isActive(rule.getIsActive())
            .cooldownMinutes(rule.getCooldownMinutes())
            .createdBy(rule.getCreatedBy())
            .createdAt(rule.getCreatedAt())
            .updatedAt(rule.getUpdatedAt())
            .build();
    }
}
