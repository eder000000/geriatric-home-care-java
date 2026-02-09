package com.geriatriccare.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Cache Configuration Tests")
class CacheConfigTest {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Test
    @DisplayName("Should have cache manager configured")
    void cacheManager_ShouldBeConfigured() {
        assertThat(cacheManager).isNotNull();
    }

    @Test
    @DisplayName("Should have dashboard cache configured")
    void dashboardCache_ShouldExist() {
        if (cacheManager != null) {
            assertThat(cacheManager.getCache("dashboardStats")).isNotNull();
        }
    }

    @Test
    @DisplayName("Should have adherence reports cache configured")
    void adherenceReportsCache_ShouldExist() {
        if (cacheManager != null) {
            assertThat(cacheManager.getCache("adherenceReports")).isNotNull();
        }
    }

    @Test
    @DisplayName("Should have medication reports cache configured")
    void medicationReportsCache_ShouldExist() {
        if (cacheManager != null) {
            assertThat(cacheManager.getCache("medicationReports")).isNotNull();
        }
    }
}
