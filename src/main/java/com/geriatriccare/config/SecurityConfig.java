package com.geriatriccare.config;

import com.geriatriccare.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                         CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Allow H2 console frames
                // Content Security Policy
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdnjs.cloudflare.com; " +
                        "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data: https://cdnjs.cloudflare.com; " +
                        "connect-src 'self' https://api.anthropic.com; " +
                        "frame-ancestors 'self'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                    )
                )
                // Strict-Transport-Security
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Swagger/OpenAPI Documentation
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Secured endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "PHYSICIAN", "CAREGIVER", "FAMILY")
                .requestMatchers("/api/medications/**").hasAnyRole("ADMIN", "PHYSICIAN", "CAREGIVER")
                .requestMatchers("/api/care-plans/**").hasAnyRole("ADMIN", "PHYSICIAN", "CAREGIVER", "FAMILY")
                .requestMatchers("/api/care-tasks/**").hasAnyRole("ADMIN", "PHYSICIAN", "CAREGIVER")
                .requestMatchers("/api/care-plan-templates/**").hasAnyRole("ADMIN", "PHYSICIAN", "CAREGIVER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
// ============================================================================
// RBAC CONFIGURATION (Sprint 6)
// ============================================================================

/**
 * Enable method-level security with @PreAuthorize
 * Already enabled in existing configuration
 */

// Permission-based authorization is now available via:
// - @PreAuthorize("@securityUtil.hasPermission('PATIENT_READ')")
// - @PreAuthorize("@securityUtil.canAccessPatient(#patientId)")
// - @PreAuthorize("hasRole('ADMIN') or @securityUtil.isAdmin()")