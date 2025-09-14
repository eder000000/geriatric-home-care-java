package com.geriatriccare.controller.api.v1;

import com.geriatriccare.dto.AuthRequest;
import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.UserRegistrationRequest;
import com.geriatriccare.entity.User;
import com.geriatriccare.entity.UserRole;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getEmail(), 
                    authRequest.getPassword()
                )
            );

            User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtTokenUtil.generateToken(user);

            return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), 
                user.getRole().name(), user.getFullName()));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Authentication failed"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email already exists"));
            }

            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
            user.setIsActive(true);

            userRepository.save(user);

            return ResponseEntity.ok(new SuccessResponse("User registered successfully"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Registration failed"));
        }
    }

    // Response classes
    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    public static class SuccessResponse {
        private String message;
        public SuccessResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}