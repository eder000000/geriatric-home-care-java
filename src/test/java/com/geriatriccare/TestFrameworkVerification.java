package com.geriatriccare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
 class TestFrameworkVerification {
    
    @Test
     void contextLoads() {
        // This test will pass if the application context loads successfully
        assertThat(true).isTrue();
    }

    @Test
    void assertWorks() {
        // Verify that assertJ is working
        String testString = "Testing assertJ";
        assertThat(testString).startsWith("Testing").endsWith("assertJ").contains("assert");
    }

    @Test
     void junitJupiterWorks() {
        // Verify that JUnit 5 is workin
        int result = 2 + 2;
        assertThat(result).isEqualTo(4);
    }
}
