package com.geriatriccare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {
    
    @Bean
    public SecurityFilterChain securityHeadersFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
            // Content Security Policy - Prevent XSS and injection attacks
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdnjs.cloudflare.com; " +
                    "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' data: https://cdnjs.cloudflare.com; " +
                    "connect-src 'self' https://api.anthropic.com; " +
                    "frame-ancestors 'none'; " +
                    "base-uri 'self'; " +
                    "form-action 'self'"
                )
            )
            
            // X-Frame-Options - Prevent clickjacking
            .frameOptions(frame -> frame.deny())
            
            // X-Content-Type-Options - Prevent MIME sniffing
            .contentTypeOptions(contentType -> contentType.disable())
            
            // Strict-Transport-Security - Force HTTPS
            .httpStrictTransportSecurity(hsts -> hsts
                .maxAgeInSeconds(31536000) // 1 year
                .includeSubDomains(true)
                .preload(true)
            )
        );
        
        // Add custom headers via response
        http.headers(headers -> headers
            .addHeaderWriter((request, response) -> {
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
                response.setHeader("X-Content-Type-Options", "nosniff");
            })
        );
        
        return http.build();
    }
}
