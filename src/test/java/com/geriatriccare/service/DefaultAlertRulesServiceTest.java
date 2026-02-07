package com.geriatriccare.service;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.repository.AlertRuleRepository;
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

    @Test
    @DisplayName("Should create 18 default alert rules on startup")
    void initializeDefaultRules_Success() {
        // Given/When - @PostConstruct runs automatically
        
        // Then
        long count = alertRuleRepository.count();
        assertThat(count).isEqualTo(18).as("Total alert rules");
        
        // Verify critical rules exist (9 critical rules)
        long criticalCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getSeverity() == AlertSeverity.CRITICAL)
            .count();
        assertThat(criticalCount).isEqualTo(9).as("Critical alert rules");
        
        // Verify warning rules exist (9 warning rules)
        long warningCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getSeverity() == AlertSeverity.WARNING)
            .count();
        assertThat(warningCount).isEqualTo(9).as("Warning alert rules");
        
        // Verify all rules are global (patientId is null)
        long globalCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getPatientId() == null)
            .count();
        assertThat(globalCount).isEqualTo(18).as("Global rules");
        
        // Verify all rules are active
        long activeCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getIsActive())
            .count();
        assertThat(activeCount).isEqualTo(18).as("Active rules");
        
        System.out.println("✅ Default Alert Rules Test Summary:");
        System.out.println("   Total Rules: " + count);
        System.out.println("   Critical: " + criticalCount);
        System.out.println("   Warning: " + warningCount);
    }
}
