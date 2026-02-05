package com.geriatriccare.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VitalSignServiceSmokeTest {

    @Test
    void contextLoads() {
        // Just verify Spring context loads with new entities
    }
}
