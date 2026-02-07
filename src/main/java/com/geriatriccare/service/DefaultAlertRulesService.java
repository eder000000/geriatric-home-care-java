package com.geriatriccare.service;

import com.geriatriccare.entity.AlertRule;
import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.enums.ComparisonOperator;
import com.geriatriccare.enums.VitalSignType;
import com.geriatriccare.repository.AlertRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Alert Rules Service
 * Creates sensible default alert thresholds for geriatric patients
 * Based on clinical guidelines for elderly care
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAlertRulesService {

    private final AlertRuleRepository alertRuleRepository;

    @PostConstruct
    @Transactional
    public void initializeDefaultRules() {
        if (alertRuleRepository.count() > 0) {
            log.info("Alert rules already exist, skipping initialization");
            return;
        }

        log.info("Initializing default alert rules for geriatric patients...");
        
        List<AlertRule> rules = createDefaultRules();
        alertRuleRepository.saveAll(rules);
        
        log.info("✅ Created {} default alert rules", rules.size());
    }

    private List<AlertRule> createDefaultRules() {
        List<AlertRule> rules = new ArrayList<>();

        // ========================================
        // BLOOD PRESSURE RULES
        // ========================================
        
        // CRITICAL: Systolic > 180 (Hypertensive Crisis)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.GREATER_THAN,
            180.0,
            null,
            "⚠️ CRITICAL: Blood pressure systolic %.0f mmHg - Hypertensive crisis risk",
            15 // 15 min cooldown for critical
        ));

        // CRITICAL: Systolic < 90 (Severe Hypotension)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            90.0,
            null,
            "⚠️ CRITICAL: Blood pressure systolic %.0f mmHg - Severe hypotension",
            15
        ));

        // WARNING: Systolic 160-180 (Stage 2 Hypertension)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            160.0,
            180.0,
            "⚠️ WARNING: Blood pressure systolic %.0f mmHg - Stage 2 hypertension",
            30
        ));

        // WARNING: Systolic 90-100 (Mild Hypotension)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            90.0,
            100.0,
            "⚠️ WARNING: Blood pressure systolic %.0f mmHg - Mild hypotension",
            30
        ));

        // ========================================
        // HEART RATE RULES
        // ========================================
        
        // CRITICAL: HR > 120 (Severe Tachycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.GREATER_THAN,
            120.0,
            null,
            "⚠️ CRITICAL: Heart rate %.0f bpm - Severe tachycardia",
            15
        ));

        // CRITICAL: HR < 50 (Severe Bradycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            50.0,
            null,
            "⚠️ CRITICAL: Heart rate %.0f bpm - Severe bradycardia",
            15
        ));

        // WARNING: HR 100-120 (Tachycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            100.0,
            120.0,
            "⚠️ WARNING: Heart rate %.0f bpm - Tachycardia",
            30
        ));

        // WARNING: HR 50-60 (Bradycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            50.0,
            60.0,
            "⚠️ WARNING: Heart rate %.0f bpm - Bradycardia",
            30
        ));

        // ========================================
        // TEMPERATURE RULES
        // ========================================
        
        // CRITICAL: Temp > 39°C (High Fever)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.GREATER_THAN,
            39.0,
            null,
            "⚠️ CRITICAL: Temperature %.1f°C - High fever",
            15
        ));

        // CRITICAL: Temp < 35°C (Hypothermia)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            35.0,
            null,
            "⚠️ CRITICAL: Temperature %.1f°C - Hypothermia risk",
            15
        ));

        // WARNING: Temp 38-39°C (Fever)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            38.0,
            39.0,
            "⚠️ WARNING: Temperature %.1f°C - Fever",
            30
        ));

        // WARNING: Temp 35-35.5°C (Low Temperature)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            35.0,
            35.5,
            "⚠️ WARNING: Temperature %.1f°C - Low body temperature",
            30
        ));

        // ========================================
        // OXYGEN SATURATION RULES
        // ========================================
        
        // CRITICAL: SpO2 < 88% (Severe Hypoxemia)
        rules.add(createRule(
            VitalSignType.OXYGEN_SATURATION,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            88.0,
            null,
            "⚠️ CRITICAL: Oxygen saturation %.0f%% - Severe hypoxemia",
            10 // Very short cooldown for O2
        ));

        // WARNING: SpO2 88-92% (Hypoxemia)
        rules.add(createRule(
            VitalSignType.OXYGEN_SATURATION,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            88.0,
            92.0,
            "⚠️ WARNING: Oxygen saturation %.0f%% - Hypoxemia",
            20
        ));

        // ========================================
        // RESPIRATORY RATE RULES
        // ========================================
        
        // CRITICAL: RR > 30 (Severe Tachypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.GREATER_THAN,
            30.0,
            null,
            "⚠️ CRITICAL: Respiratory rate %.0f breaths/min - Severe tachypnea",
            15
        ));

        // CRITICAL: RR < 8 (Severe Bradypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            8.0,
            null,
            "⚠️ CRITICAL: Respiratory rate %.0f breaths/min - Severe bradypnea",
            15
        ));

        // WARNING: RR 24-30 (Tachypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            24.0,
            30.0,
            "⚠️ WARNING: Respiratory rate %.0f breaths/min - Tachypnea",
            30
        ));

        // WARNING: RR 8-12 (Bradypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            8.0,
            12.0,
            "⚠️ WARNING: Respiratory rate %.0f breaths/min - Bradypnea",
            30
        ));

        return rules;
    }

    private AlertRule createRule(
            VitalSignType type,
            AlertSeverity severity,
            ComparisonOperator operator,
            Double threshold,
            Double thresholdMax,
            String message,
            Integer cooldownMinutes) {
        
        AlertRule rule = new AlertRule();
        rule.setPatientId(null); // Global rule
        rule.setVitalSignType(type);
        rule.setSeverity(severity);
        rule.setComparisonOperator(operator);
        rule.setThresholdValue(threshold);
        rule.setThresholdValueMax(thresholdMax);
        rule.setAlertMessage(message);
        rule.setIsActive(true);
        rule.setCooldownMinutes(cooldownMinutes);
        
        return rule;
    }
}
