package com.geriatriccare;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GeriatricHomeCareApplicationTests {

    @Test
    @Disabled("Disabled until further notice")
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
    }
}