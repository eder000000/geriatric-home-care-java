package com.geriatriccare.service;

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
    @DisplayName("Should create 20 default alert rules on startup")
    void initializeDefaultRules_Success() {
        // Given/When - @PostConstruct runs automatically
        
        // Then
        long count = alertRuleRepository.count();
        assertThat(count).isEqualTo(20);
        
        // Verify critical rules exist
        long criticalCount = alertRuleRepository.findAll().stream()
            .filter(rule -> rule.getSeverity().name().equals("CRITICAL"))
            .count();
        assertThat(criticalCount).isGreaterThan(0);
    }
}
