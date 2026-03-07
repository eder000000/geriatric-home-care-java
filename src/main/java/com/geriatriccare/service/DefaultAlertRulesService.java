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
            "⚠️ CRÍTICO: Presión arterial sistólica %.0f mmHg - Riesgo de crisis hipertensiva",
            15 // 15 min cooldown for critical
        ));

        // CRITICAL: Systolic < 90 (Severe Hypotension)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            90.0,
            null,
            "⚠️ CRÍTICO: Presión arterial sistólica %.0f mmHg - Hipotensión severa",
            15
        ));

        // WARNING: Systolic 160-180 (Stage 2 Hypertension)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            160.0,
            180.0,
            "⚠️ ADVERTENCIA: Presión arterial sistólica %.0f mmHg - Hipertensión etapa 2",
            30
        ));

        // WARNING: Systolic 90-100 (Mild Hypotension)
        rules.add(createRule(
            VitalSignType.BLOOD_PRESSURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            90.0,
            100.0,
            "⚠️ ADVERTENCIA: Presión arterial sistólica %.0f mmHg - Hipotensión leve",
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
            "⚠️ CRÍTICO: Frecuencia cardíaca %.0f lpm - Taquicardia severa",
            15
        ));

        // CRITICAL: HR < 50 (Severe Bradycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            50.0,
            null,
            "⚠️ CRÍTICO: Frecuencia cardíaca %.0f lpm - Bradicardia severa",
            15
        ));

        // WARNING: HR 100-120 (Tachycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            100.0,
            120.0,
            "⚠️ ADVERTENCIA: Frecuencia cardíaca %.0f lpm - Taquicardia",
            30
        ));

        // WARNING: HR 50-60 (Bradycardia)
        rules.add(createRule(
            VitalSignType.HEART_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            50.0,
            60.0,
            "⚠️ ADVERTENCIA: Frecuencia cardíaca %.0f lpm - Bradicardia",
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
            "⚠️ CRÍTICO: Temperatura %.1f°C - Fiebre alta",
            15
        ));

        // CRITICAL: Temp < 35°C (Hypothermia)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            35.0,
            null,
            "⚠️ CRÍTICO: Temperatura %.1f°C - Riesgo de hipotermia",
            15
        ));

        // WARNING: Temp 38-39°C (Fever)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            38.0,
            39.0,
            "⚠️ ADVERTENCIA: Temperatura %.1f°C - Fiebre moderada",
            30
        ));

        // WARNING: Temp 35-35.5°C (Low Temperature)
        rules.add(createRule(
            VitalSignType.TEMPERATURE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            35.0,
            35.5,
            "⚠️ ADVERTENCIA: Temperatura %.1f°C - Temperatura corporal baja",
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
            "⚠️ CRÍTICO: Saturación de oxígeno %.0f%% - Hipoxia severa",
            10 // Very short cooldown for O2
        ));

        // WARNING: SpO2 88-92% (Hypoxemia)
        rules.add(createRule(
            VitalSignType.OXYGEN_SATURATION,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            88.0,
            92.0,
            "⚠️ ADVERTENCIA: Saturación de oxígeno %.0f%% - Hipoxia leve",
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
            "⚠️ CRÍTICO: Frecuencia respiratoria %.0f rpm - Taquipnea severa",
            15
        ));

        // CRITICAL: RR < 8 (Severe Bradypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.CRITICAL,
            ComparisonOperator.LESS_THAN,
            8.0,
            null,
            "⚠️ CRÍTICO: Frecuencia respiratoria %.0f rpm - Bradipnea severa",
            15
        ));

        // WARNING: RR 24-30 (Tachypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            24.0,
            30.0,
            "⚠️ ADVERTENCIA: Frecuencia respiratoria %.0f rpm - Taquipnea",
            30
        ));

        // WARNING: RR 8-12 (Bradypnea)
        rules.add(createRule(
            VitalSignType.RESPIRATORY_RATE,
            AlertSeverity.WARNING,
            ComparisonOperator.BETWEEN,
            8.0,
            12.0,
            "⚠️ ADVERTENCIA: Frecuencia respiratoria %.0f rpm - Bradipnea",
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
