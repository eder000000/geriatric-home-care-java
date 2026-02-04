package com.geriatriccare.unit.service.ai;

import com.geriatriccare.config.OpenAIConfig;
import com.geriatriccare.dto.ai.Message;
import com.geriatriccare.dto.ai.OpenAIResponse;
import com.geriatriccare.service.ai.AIAuditLogService;
import com.geriatriccare.service.ai.OpenAIService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OpenAIService Tests")
class OpenAIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AIAuditLogService auditLogService;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private OpenAIService openAIService;
    private OpenAIConfig.OpenAIProperties properties;

    @BeforeEach
    void setUp() {
        properties = new OpenAIConfig.OpenAIProperties();
        properties.setApiKey("test-api-key");
        properties.setApiUrl("https://api.openai.com/v1/chat/completions");
        properties.setModel("gpt-4");
        properties.setMaxTokens(2000);
        properties.setTemperature(0.3);
        properties.setTimeout(30000);
        properties.setMaxRetries(1);  // Reduce retries for faster tests
        properties.setRetryDelay(100);

        when(circuitBreakerRegistry.circuitBreaker(anyString()))
                .thenReturn(CircuitBreakerRegistry.ofDefaults().circuitBreaker("test"));

        openAIService = new OpenAIService(
                restTemplate,
                properties,
                auditLogService,
                circuitBreakerRegistry
        );
    }

    @Nested
    @DisplayName("Generate Completion Tests")
    class GenerateCompletionTests {

        @Test
        @DisplayName("Should generate completion successfully")
        void generateCompletion_Success() {
            // Arrange
            String prompt = "What medication for hypertension?";
            String context = "You are a medical AI assistant.";

            OpenAIResponse mockResponse = createMockResponse("I recommend Lisinopril.", 150);
            
            when(restTemplate.postForEntity(
                    eq(properties.getApiUrl()),
                    any(),
                    eq(OpenAIResponse.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            // Act
            OpenAIResponse response = openAIService.generateCompletion(prompt, context);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getChoices()).hasSize(1);
            assertThat(response.getChoices().get(0).getMessage().getContent())
                    .isEqualTo("I recommend Lisinopril.");
            assertThat(response.getUsage().getTotalTokens()).isEqualTo(150);

            verify(auditLogService).logAIRequest(eq(prompt), eq(mockResponse));
            verify(restTemplate).postForEntity(
                    eq(properties.getApiUrl()),
                    any(),
                    eq(OpenAIResponse.class));
        }

        @Test
        @DisplayName("Should handle custom temperature")
        void generateCompletion_WithCustomTemperature() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";
            double customTemp = 0.7;

            OpenAIResponse mockResponse = createMockResponse("Test response", 100);
            
            when(restTemplate.postForEntity(
                    eq(properties.getApiUrl()),
                    any(),
                    eq(OpenAIResponse.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            // Act
            OpenAIResponse response = openAIService.generateCompletion(prompt, context, customTemp);

            // Assert
            assertThat(response).isNotNull();
            verify(auditLogService).logAIRequest(eq(prompt), eq(mockResponse));
        }

        @Test
        @DisplayName("Should log audit on successful request")
        void generateCompletion_LogsAudit() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";
            OpenAIResponse mockResponse = createMockResponse("Test response", 100);

            when(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(OpenAIResponse.class)))
                    .thenReturn(ResponseEntity.ok(mockResponse));

            // Act
            openAIService.generateCompletion(prompt, context);

            // Assert
            verify(auditLogService, times(1)).logAIRequest(prompt, mockResponse);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle rate limit error (429)")
        void generateCompletion_RateLimitError() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OpenAIResponse.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

            // Act & Assert
            assertThatThrownBy(() -> openAIService.generateCompletion(prompt, context))
                    .isInstanceOf(OpenAIService.AIRateLimitException.class)
                    .hasMessageContaining("rate limit");

            verify(auditLogService).logAIError(eq(prompt), anyString());
        }

        @Test
        @DisplayName("Should handle authentication error (401)")
        void generateCompletion_AuthenticationError() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OpenAIResponse.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            // Act & Assert
            assertThatThrownBy(() -> openAIService.generateCompletion(prompt, context))
                    .isInstanceOf(OpenAIService.AIAuthenticationException.class)
                    .hasMessageContaining("API key");

            verify(auditLogService).logAIError(eq(prompt), anyString());
        }

        @Test
        @DisplayName("Should handle server error (500)")
        void generateCompletion_ServerError() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OpenAIResponse.class)))
                    .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            // Act & Assert
            assertThatThrownBy(() -> openAIService.generateCompletion(prompt, context))
                    .isInstanceOf(OpenAIService.AIServiceException.class)
                    .hasMessageContaining("server error");

            verify(auditLogService).logAIError(eq(prompt), anyString());
        }

        @Test
        @DisplayName("Should handle empty response")
        void generateCompletion_EmptyResponse() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OpenAIResponse.class)))
                    .thenReturn(ResponseEntity.ok(null));

            // Act & Assert
            assertThatThrownBy(() -> openAIService.generateCompletion(prompt, context))
                    .isInstanceOf(OpenAIService.AIServiceException.class)
                    .hasMessageContaining("Empty response");
        }

        @Test
        @DisplayName("Should log error on failed request")
        void generateCompletion_LogsError() {
            // Arrange
            String prompt = "Test prompt";
            String context = "Test context";
            String errorMessage = "API Error";

            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OpenAIResponse.class)))
                    .thenThrow(new RuntimeException(errorMessage));

            // Act & Assert
            assertThatThrownBy(() -> openAIService.generateCompletion(prompt, context))
                    .isInstanceOf(OpenAIService.AIServiceException.class);

            verify(auditLogService).logAIError(eq(prompt), anyString());
        }
    }

    @Nested
    @DisplayName("Service Availability Tests")
    class ServiceAvailabilityTests {

        @Test
        @DisplayName("Should check if service is available")
        void isServiceAvailable_ReturnsTrue() {
            // Act
            boolean available = openAIService.isServiceAvailable();

            // Assert
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("Should get circuit breaker state")
        void getCircuitBreakerState_ReturnsClosed() {
            // Act
            String state = openAIService.getCircuitBreakerState();

            // Assert
            assertThat(state).isEqualTo("CLOSED");
        }
    }

    // Helper method to create mock response
    private OpenAIResponse createMockResponse(String content, int totalTokens) {
        OpenAIResponse response = new OpenAIResponse();
        response.setId("chatcmpl-123");
        response.setModel("gpt-4");
        response.setCreated(System.currentTimeMillis() / 1000);

        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        choice.setIndex(0);
        choice.setMessage(new Message("assistant", content));
        choice.setFinishReason("stop");

        response.setChoices(List.of(choice));

        OpenAIResponse.Usage usage = new OpenAIResponse.Usage();
        usage.setPromptTokens(totalTokens / 2);
        usage.setCompletionTokens(totalTokens / 2);
        usage.setTotalTokens(totalTokens);

        response.setUsage(usage);

        return response;
    }
}