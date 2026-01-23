package com.geriatriccare.config;

import com.geriatriccare.service.ai.OpenAIService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class OpenAITestConfig {
    @Bean
    @Primary
    public OpenAIService mockOpenAIService() {
        return Mockito.mock(OpenAIService.class);
    }
}
