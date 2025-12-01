package com.geriatriccare.service.ai;

import com.geriatriccare.config.OpenAIConfig;
import com.geriatriccare.dto.ai.Message;
import com.geriatriccare.dto.ai.OpenAIRequest;
import com.geriatriccare.dto.ai.OpenAIResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Service
public class OpenAIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    private final RestTemplate restTemplate;
    private final OpenAIConfig.OpenAIProperties properties;
    private final AIAuditLogService auditLogService;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public OpenAIService(
            RestTemplate openAIRestTemplate,
            OpenAIConfig.OpenAIProperties properties,
            AIAuditLogService auditLogService,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        
        this.restTemplate = openAIRestTemplate;
        this.properties = properties;
        this.auditLogService = auditLogService;
        
        // Initialize Circuit Breaker
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("openai");
        
        // Initialize Retry
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(properties.getMaxRetries())
                .waitDuration(Duration.ofMillis(properties.getRetryDelay()))
                .retryOnException(e -> 
                    e instanceof HttpServerErrorException || 
                    e instanceof java.net.SocketTimeoutException)
                .build();
        this.retry = Retry.of("openai", retryConfig);
    }

    /**
     * Generate AI completion using OpenAI API
     * 
     * @param prompt The user prompt
     * @param context System context/instructions
     * @return OpenAI API response
     */
    public OpenAIResponse generateCompletion(String prompt, String context) {
        log.info("Generating AI completion for prompt length: {}", prompt.length());
        
        try {
            // Build request
            OpenAIRequest request = buildRequest(prompt, context);
            
            // Execute with circuit breaker and retry
            Supplier<OpenAIResponse> supplier = () -> executeRequest(request);
            Supplier<OpenAIResponse> decoratedSupplier = CircuitBreaker
                    .decorateSupplier(circuitBreaker, 
                        Retry.decorateSupplier(retry, supplier));
            
            OpenAIResponse response = decoratedSupplier.get();
            
            // Log for audit
            auditLogService.logAIRequest(prompt, response);
            
            log.info("AI completion generated successfully. Tokens used: {}", 
                    response.getUsage().getTotalTokens());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            auditLogService.logAIError(prompt, e.getMessage());
            throw new AIServiceException("Failed to generate AI completion", e);
        }
    }

    /**
     * Generate AI completion with custom temperature
     */
    public OpenAIResponse generateCompletion(String prompt, String context, double temperature) {
        OpenAIRequest request = OpenAIRequest.builder()
                .model(properties.getModel())
                .messages(buildMessages(prompt, context))
                .maxTokens(properties.getMaxTokens())
                .temperature(temperature)
                .build();
        
        return executeRequestWithRetry(request, prompt);
    }

    /**
     * Check if OpenAI service is available
     */
    public boolean isServiceAvailable() {
        return circuitBreaker.getState() == CircuitBreaker.State.CLOSED;
    }

    /**
     * Get current circuit breaker state
     */
    public String getCircuitBreakerState() {
        return circuitBreaker.getState().toString();
    }

    // ==================== Private Helper Methods ====================

    private OpenAIRequest buildRequest(String prompt, String context) {
        return OpenAIRequest.builder()
                .model(properties.getModel())
                .messages(buildMessages(prompt, context))
                .maxTokens(properties.getMaxTokens())
                .temperature(properties.getTemperature())
                .build();
    }

    private List<Message> buildMessages(String prompt, String context) {
        return Arrays.asList(
            new Message("system", context),
            new Message("user", prompt)
        );
    }

    private OpenAIResponse executeRequest(OpenAIRequest request) {
        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<OpenAIRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            // Call OpenAI API
            ResponseEntity<OpenAIResponse> response = restTemplate.postForEntity(
                properties.getApiUrl(),
                entity,
                OpenAIResponse.class
            );
            
            if (response.getBody() == null) {
                throw new AIServiceException("Empty response from OpenAI API");
            }
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Rate limit exceeded. Status: {}", e.getStatusCode());
                throw new AIRateLimitException("OpenAI rate limit exceeded", e);
            } else if (e.getStatusCode().value() == 401) {
                log.error("Invalid API key");
                throw new AIAuthenticationException("Invalid OpenAI API key", e);
            }
            throw new AIServiceException("OpenAI API error: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("OpenAI server error. Status: {}", e.getStatusCode());
            throw new AIServiceException("OpenAI server error", e);
        }
    }

    private OpenAIResponse executeRequestWithRetry(OpenAIRequest request, String prompt) {
        try {
            Supplier<OpenAIResponse> supplier = () -> executeRequest(request);
            Supplier<OpenAIResponse> decoratedSupplier = CircuitBreaker
                    .decorateSupplier(circuitBreaker, 
                        Retry.decorateSupplier(retry, supplier));
            
            OpenAIResponse response = decoratedSupplier.get();
            auditLogService.logAIRequest(prompt, response);
            return response;
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API after retries", e);
            auditLogService.logAIError(prompt, e.getMessage());
            throw new AIServiceException("Failed to generate AI completion", e);
        }
    }

    // ==================== Custom Exceptions ====================

    public static class AIServiceException extends RuntimeException {
        public AIServiceException(String message) {
            super(message);
        }
        
        public AIServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AIRateLimitException extends AIServiceException {
        public AIRateLimitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AIAuthenticationException extends AIServiceException {
        public AIAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}