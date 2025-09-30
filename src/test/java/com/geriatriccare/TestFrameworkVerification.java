package com.geriatriccare;

import org.junit.jupiter.api.Test;  // CORRECT IMPORT
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TestFrameworkVerification {  // Added public
    
    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
    
    @Test
    void assertJWorks() {
        String testString = "Testing Framework";
        assertThat(testString)
            .isNotNull()
            .startsWith("Testing")
            .endsWith("Framework");
    }
    
    @Test
    void junitJupiterWorks() {
        int result = 2 + 2;
        assertThat(result).isEqualTo(4);
    }
}