package com.geriatriccare.service;

import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import com.geriatriccare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.default.owner.email:owner@geriatriccare.com}")
    private String defaultOwnerEmail;
    
    @Value("${app.default.owner.password:Owner123!}")
    private String defaultOwnerPassword;
    
    @Value("${app.default.owner.firstName:System}")
    private String defaultOwnerFirstName;
    
    @Value("${app.default.owner.lastName:Owner}")
    private String defaultOwnerLastName;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        createDefaultOwnerIfNotExists();
    }
    
    private void createDefaultOwnerIfNotExists() {
        logger.info("Checking for default owner user...");
        
        // Check if any OWNER user exists
        boolean ownerExists = userRepository.existsByRoleAndIsActiveTrue(UserRole.ADMIN);
        
        if (!ownerExists) {
            logger.info("No OWNER user found. Creating default owner user...");
            
            User owner = new User();
            owner.setUsername("admin");
            owner.setFirstName(defaultOwnerFirstName);
            owner.setLastName(defaultOwnerLastName);
            owner.setEmail(defaultOwnerEmail);
            owner.setPassword(passwordEncoder.encode(defaultOwnerPassword));
            owner.setRole(UserRole.ADMIN);
            owner.setStatus(UserStatus.ACTIVE);
            
            User savedOwner = userRepository.save(owner);
            
            logger.info("✅ Default OWNER user created successfully!");
            logger.info("   Email: {}", savedOwner.getEmail());
            logger.info("   Password: {} (Change this in production!)", defaultOwnerPassword);
            logger.info("   ID: {}", savedOwner.getId());
            
        } else {
            logger.info("✅ OWNER user already exists. Skipping creation.");
        }
    }
}