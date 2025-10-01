package com.geriatriccare.base;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceTestImpl extends BaseServiceTest {
    @Test
    void baseServiceTestWorks() {
        RuntimeException ex = testException("test");
        assertThat(ex.getMessage()).contains("Test exception: test");
    }
}

class IntegrationTestImpl extends BaseIntegrationTest {
    @Test
    void baseIntegrationTestWorks() {
        assertThat(baseUrl).startsWith("http://localhost:");
        assertThat(ownerToken).isNotNull();
        assertThat(caregiverToken).isNotNull();
    }
}