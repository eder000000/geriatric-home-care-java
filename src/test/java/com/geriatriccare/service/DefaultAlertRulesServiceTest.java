package com.geriatriccare.service;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.repository.AlertRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DefaultAlertRulesServiceTest {

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private DefaultAlertRulesService defaultAlertRulesService;

    @BeforeEach
    void setUp() {
        // Clear any existing rules, then re-initialize fresh for this test
        alertRuleRepository.deleteAll();
        defaultAlertRulesService.initializeDefaultRules();
    }

    @Test
    @DisplayName("Should create 18 default alert rules on initialization")
    void initializeDefaultRules_Success() {
        long count = alertRuleRepository.count();
        assertThat(count).isEqualTo(18).as("Total alert rules");

        long criticalCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getSeverity() == AlertSeverity.CRITICAL)
            .count();
        assertThat(criticalCount).isEqualTo(9).as("Critical alert rules");

        long warningCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getSeverity() == AlertSeverity.WARNING)
            .count();
        assertThat(warningCount).isEqualTo(9).as("Warning alert rules");

        long globalCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getPatientId() == null)
            .count();
        assertThat(globalCount).isEqualTo(18).as("Global rules");

        long activeCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getIsActive())
            .count();
        assertThat(activeCount).isEqualTo(18).as("Active rules");
    }
}
