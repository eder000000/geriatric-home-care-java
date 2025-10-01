package com.geriatriccare.base;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseServiceTest {
    
    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }
    
    // Helper method to create a standard test exception
    protected RuntimeException testException(String message) {
        return new RuntimeException("Test exception: " + message);
    }
}