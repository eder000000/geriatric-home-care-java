package com.geriatriccare.service;

import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Register a new user
     */
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        logger.info("Attempting to register user with email: {}", registerRequest.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmailAndIsActiveTrue(registerRequest.getEmail())) {
            throw new RuntimeException("Email address already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Generate JWT token
        String jwt = jwtUtil.generateTokenFromUsername(savedUser.getEmail());
        LocalDateTime expiresAt = jwtUtil.getExpirationFromToken(jwt);
        
        return new AuthResponse(
            jwt,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole(),
            expiresAt
        );
    }
    
    /**
     * Authenticate user and generate token
     */
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());
        
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            // Get user details
            User user = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate JWT token
            String jwt = jwtUtil.generateTokenFromAuthentication(authentication);
            LocalDateTime expiresAt = jwtUtil.getExpirationFromToken(jwt);
            
            logger.info("User authenticated successfully: {}", user.getEmail());
            
            return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                expiresAt
            );
            
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password!");
        }
    }
    
    /**
     * Validate JWT token and return user info
     */
    public User validateToken(String token) {
        if (!jwtUtil.validateJwtToken(token)) {
            throw new BadCredentialsException("Invalid or expired token");
        }
        
        String email = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByEmailAndIsActiveTrue(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Refresh JWT token
     */
    public AuthResponse refreshToken(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String jwt = jwtUtil.generateTokenFromUsername(user.getEmail());
        LocalDateTime expiresAt = jwtUtil.getExpirationFromToken(jwt);
        
        return new AuthResponse(
            jwt,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            expiresAt
        );
    }
    
    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndIsActiveTrue(email);
    }
}