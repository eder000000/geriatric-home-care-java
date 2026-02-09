package com.geriatriccare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration
 * Configures Swagger/OpenAPI documentation
 * Phase 1: Quick Wins
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI geriatricCareOpenAPI() {
        // Security scheme for JWT
        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

        // Security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList("bearerAuth");

        // Server configurations
        Server localServer = new Server()
            .url("http://localhost:8080")
            .description("Local Development Server");

        Server prodServer = new Server()
            .url("https://api.geriatriccare.com")
            .description("Production Server");

        // Contact information
        Contact contact = new Contact()
            .name("Geriatric Care Support")
            .email("support@geriatriccare.com")
            .url("https://github.com/eder000000/geriatric-home-care-java");

        // License
        License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

        // API Info
        Info info = new Info()
            .title("Geriatric Home Care Management API")
            .version("v1.8.0")
            .description("""
                ## Comprehensive API for Geriatric Patient Care Management
                
                This API provides complete functionality for managing elderly patient care including:
                
                ### Core Features
                - **Patient Management** - CRUD operations for patient records
                - **Care Plan Management** - Create and manage coordinated care plans
                - **Care Task Tracking** - Schedule and track care tasks
                - **Medication Management** - Prescription and inventory management
                - **Vital Signs Monitoring** - Real-time vital sign tracking (BP, HR, Temp, SpO2, RR)
                - **Automated Alerting** - 18 default alert rules with intelligent thresholds
                - **Adherence Reporting** - Care plan and medication adherence with pattern detection
                - **Health Dashboards** - Population-level health outcome metrics
                - **Export Capabilities** - PDF and CSV report generation
                
                ### Authentication
                Most endpoints require authentication using JWT tokens. Include the token in the Authorization header:
```
                Authorization: Bearer <your-jwt-token>
```
                
                ### Rate Limiting
                API calls are rate-limited to prevent abuse. Default limits:
                - Authenticated users: 1000 requests/hour
                - Unauthenticated users: 100 requests/hour
                
                ### Pagination
                List endpoints support pagination with query parameters:
                - `page` - Page number (0-indexed, default: 0)
                - `size` - Page size (default: 20, max: 100)
                
                ### Error Handling
                The API uses standard HTTP status codes:
                - `200` - Success
                - `201` - Created
                - `400` - Bad Request
                - `401` - Unauthorized
                - `403` - Forbidden
                - `404` - Not Found
                - `500` - Internal Server Error
                
                ### Support
                For support, contact support@geriatriccare.com or visit our [GitHub repository](https://github.com/eder000000/geriatric-home-care-java).
                """)
            .contact(contact)
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer, prodServer))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", securityScheme))
            .addSecurityItem(securityRequirement);
    }
}
