package com.geriatriccare.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class TestConfigurationVerification {
    
    @Autowired
    private Environment env;
    
    @Test
    void testProfileLoads() {
        // Check which profiles are active
        String[] profiles = env.getActiveProfiles();
        assertThat(profiles).contains("test");
        
        // Check a property that should exist
        String datasourceUrl = env.getProperty("spring.datasource.url");
        System.out.println("Datasource URL: " + datasourceUrl);
        
        // This will be null if not found, not throw error
        String jwtSecret = env.getProperty("jwt.secret");
        System.out.println("JWT Secret: " + jwtSecret);
    }
}