package com.geriatriccare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
  
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // This enables automatic auditing for @CreatedDate and @LastModifiedDate
    // When entities are saved, Spring Data JPA will automatically populate these fields
}
