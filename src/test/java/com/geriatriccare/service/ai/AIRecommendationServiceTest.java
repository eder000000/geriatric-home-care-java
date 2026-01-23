package com.geriatriccare.service.ai;

import com.geriatriccare.config.OpenAITestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(OpenAITestConfig.class)
class AIRecommendationServiceTest {
    @MockBean
    private OpenAIService openAIService;

    @Test
    @DisplayName("OpenAI service mocked")
    void openAIServiceMocked() {
        when(openAIService.generateCompletion(anyString(), anyString()))
            .thenReturn("Mocked response");
    }
}
