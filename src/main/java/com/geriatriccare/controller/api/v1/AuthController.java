package com.geriatriccare.controller.api.v1;

import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.ErrorResponse;
import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.security.UserPrincipal;
import com.geriatriccare.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration request received for email: {}", registerRequest.getEmail());
            
            AuthResponse authResponse = authService.registerUser(registerRequest);
            
            logger.info("User registered successfully: {}", registerRequest.getEmail());
            return ResponseEntity.ok(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Registration Failed",
                e.getMessage(),
                "/api/auth/register"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred during registration",
                "/api/auth/register"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Authenticate user login
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login request received for email: {}", loginRequest.getEmail());
            
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            
            logger.info("User logged in successfully: {}", loginRequest.getEmail());
            return ResponseEntity.ok(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Login failed: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                e.getMessage(),
                "/api/auth/login"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred during login",
                "/api/auth/login"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            if (userPrincipal == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "Authentication required",
                    "/api/auth/me"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userPrincipal.getId());
            userInfo.put("firstName", userPrincipal.getFirstName());
            userInfo.put("lastName", userPrincipal.getLastName());
            userInfo.put("email", userPrincipal.getEmail());
            userInfo.put("authorities", userPrincipal.getAuthorities());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                "/api/auth/me"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            if (userPrincipal == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "Authentication required",
                    "/api/auth/refresh"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            AuthResponse authResponse = authService.refreshToken(userPrincipal.getEmail());
            return ResponseEntity.ok(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token Refresh Failed",
                e.getMessage(),
                "/api/auth/refresh"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred during token refresh",
                "/api/auth/refresh"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Check if email is available
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = authService.existsByEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("available", !exists);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking email availability: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                "/api/auth/check-email"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Logout (client-side token removal)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
}