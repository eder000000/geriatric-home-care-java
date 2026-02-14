#!/bin/bash

echo "🚀 Creating Sprint 7 files (matching your project structure)..."
echo ""

# ============================================================================
# 1. CREATE MISSING EXCEPTION CLASS
# ============================================================================

echo "📝 Creating missing exception..."
mkdir -p src/main/java/com/geriatriccare/exception

cat > src/main/java/com/geriatriccare/exception/InvalidPasswordException.java << 'EOF'
package com.geriatriccare.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
EOF
echo "✅ InvalidPasswordException.java"

cat > src/main/java/com/geriatriccare/exception/ResourceNotFoundException.java << 'EOF'
package com.geriatriccare.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
EOF
echo "✅ ResourceNotFoundException.java"

# ============================================================================
# 2. CREATE PASSWORD HISTORY MODEL & REPOSITORY
# ============================================================================

echo ""
echo "📝 Creating PasswordHistory entity..."

cat > src/main/java/com/geriatriccare/entity/PasswordHistory.java << 'EOF'
package com.geriatriccare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_history")
public class PasswordHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
EOF
echo "✅ PasswordHistory.java"

cat > src/main/java/com/geriatriccare/repository/PasswordHistoryRepository.java << 'EOF'
package com.geriatriccare.repository;

import com.geriatriccare.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {
    List<PasswordHistory> findByUserIdOrderByChangedAtDesc(UUID userId);
}
EOF
echo "✅ PasswordHistoryRepository.java"

# ============================================================================
# 3. UPDATE EXISTING FILES TO MATCH YOUR STRUCTURE
# ============================================================================

echo ""
echo "📝 Updating PasswordPolicyService (matching your structure)..."

cat > src/main/java/com/geriatriccare/service/security/PasswordPolicyService.java << 'EOF'
package com.geriatriccare.service.security;

import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.entity.PasswordHistory;
import com.geriatriccare.repository.PasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int PASSWORD_HISTORY_SIZE = 5;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Autowired private PasswordHistoryRepository passwordHistoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public void validatePassword(String password, String username) {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (!UPPERCASE.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain uppercase letter");
        }
        if (!LOWERCASE.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain lowercase letter");
        }
        if (!DIGIT.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain digit");
        }
        if (!SPECIAL.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain special character");
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            throw new InvalidPasswordException("Password cannot be same as username");
        }
    }

    public boolean isPasswordInHistory(UUID userId, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
        int checkCount = Math.min(PASSWORD_HISTORY_SIZE, history.size());
        for (int i = 0; i < checkCount; i++) {
            if (passwordEncoder.matches(newPassword, history.get(i).getPasswordHash())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void addToPasswordHistory(UUID userId, String passwordHash) {
        PasswordHistory history = new PasswordHistory();
        history.setUserId(userId);
        history.setPasswordHash(passwordHash);
        history.setChangedAt(LocalDateTime.now());
        passwordHistoryRepository.save(history);

        List<PasswordHistory> allHistory = passwordHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
        if (allHistory.size() > PASSWORD_HISTORY_SIZE) {
            passwordHistoryRepository.deleteAll(allHistory.subList(PASSWORD_HISTORY_SIZE, allHistory.size()));
        }
    }
}
EOF
echo "✅ PasswordPolicyService.java (updated)"

echo ""
echo "📝 Updating UserService (matching your structure)..."

cat > src/main/java/com/geriatriccare/service/UserService.java << 'EOF'
package com.geriatriccare.service;

import com.geriatriccare.dto.user.ChangePasswordRequest;
import com.geriatriccare.dto.user.UserRequest;
import com.geriatriccare.dto.user.UserResponse;
import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.service.security.PasswordPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PasswordPolicyService passwordPolicyService;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        passwordPolicyService.validatePassword(request.getPassword(), request.getEmail());

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setIsActive(true);
        user.setPasswordChangedAt(LocalDateTime.now());
        user = userRepository.save(user);

        passwordPolicyService.addToPasswordHistory(user.getId(), user.getPassword());
        return convertToResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password incorrect");
        }

        passwordPolicyService.validatePassword(request.getNewPassword(), user.getEmail());

        if (passwordPolicyService.isPasswordInHistory(userId, request.getNewPassword())) {
            throw new InvalidPasswordException("Cannot reuse last 5 passwords");
        }

        String encoded = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encoded);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordPolicyService.addToPasswordHistory(userId, encoded);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return convertToResponse(userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByIsActiveTrue().stream()
            .map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setFirstName(user.getFirstName());
        r.setLastName(user.getLastName());
        r.setEmail(user.getEmail());
        r.setPhone(user.getPhone());
        r.setRole(user.getRole().name());
        r.setIsActive(user.getIsActive());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
EOF
echo "✅ UserService.java (updated)"

# ============================================================================
# 4. CREATE TEST FILES
# ============================================================================

echo ""
echo "📝 Creating test files..."

cat > src/test/java/com/geriatriccare/controller/AuthApiTest.java << 'EOF'
package com.geriatriccare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private User testUser;
    private String validPassword = "Test123!@#";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode(validPassword));
        testUser.setRole(UserRole.CAREGIVER);
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register new user")
    void registerUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("new@example.com");
        request.setPassword("NewUser123!@#");
        request.setRole(UserRole.CAREGIVER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should login successfully")
    void loginUser_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testUser.getEmail());
        request.setPassword(validPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("GET /api/auth/me - Should return current user")
    void getCurrentUser_Success() throws Exception {
        String token = jwtUtil.generateToken(testUser.getEmail(), testUser.getRole().name());
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }
}
EOF
echo "✅ AuthApiTest.java"

cat > src/test/java/com/geriatriccare/controller/PatientApiTest.java << 'EOF'
package com.geriatriccare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatientRepository patientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private User caregiverUser;
    private Patient testPatient;
    private String caregiverToken;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        userRepository.deleteAll();

        caregiverUser = new User();
        caregiverUser.setFirstName("Caregiver");
        caregiverUser.setLastName("Test");
        caregiverUser.setEmail("caregiver@test.com");
        caregiverUser.setPassword(passwordEncoder.encode("Test123!@#"));
        caregiverUser.setRole(UserRole.CAREGIVER);
        caregiverUser.setIsActive(true);
        caregiverUser = userRepository.save(caregiverUser);
        caregiverToken = jwtUtil.generateToken(caregiverUser.getEmail(), caregiverUser.getRole().name());

        testPatient = new Patient();
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setDateOfBirth(LocalDate.of(1945, 5, 15));
        testPatient.setGender("Male");
        testPatient.setPhone("555-1234");
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    @DisplayName("POST /api/patients - Should create patient")
    void createPatient_Success() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName("Sarah");
        request.setLastName("Smith");
        request.setDateOfBirth(LocalDate.of(1950, 3, 20));
        request.setGender("Female");
        request.setPhone("555-9999");

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + caregiverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Sarah"));
    }

    @Test
    @DisplayName("GET /api/patients - Should return all patients")
    void getAllPatients_Success() throws Exception {
        mockMvc.perform(get("/api/patients")
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Should return patient")
    void getPatientById_Success() throws Exception {
        mockMvc.perform(get("/api/patients/" + testPatient.getId())
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }
}
EOF
echo "✅ PatientApiTest.java"

mkdir -p src/test/java/com/geriatriccare/config
cat > src/test/java/com/geriatriccare/config/OpenAITestConfig.java << 'EOF'
package com.geriatriccare.config;

import com.geriatriccare.service.ai.OpenAIService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class OpenAITestConfig {
    @Bean
    @Primary
    public OpenAIService mockOpenAIService() {
        return Mockito.mock(OpenAIService.class);
    }
}
EOF
echo "✅ OpenAITestConfig.java"

mkdir -p src/test/java/com/geriatriccare/service/ai
cat > src/test/java/com/geriatriccare/service/ai/AIRecommendationServiceTest.java << 'EOF'
package com.geriatriccare.service.ai;

import com.geriatriccare.config.OpenAITestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(OpenAITestConfig.class)
class AIRecommendationServiceTest {
    @MockBean
    private OpenAIService openAIService;

    @Test
    @DisplayName("OpenAI service mocked")
    void openAIServiceMocked() {
        when(openAIService.generateCompletion(anyString(), anyString()))
            .thenReturn("Mocked response");
    }
}
EOF
echo "✅ AIRecommendationServiceTest.java"

# ============================================================================
# 5. BUILD & TEST
# ============================================================================

echo ""
echo "🔨 Compiling..."
mvn clean compile -DskipTests

echo ""
echo "🧪 Running tests..."
mvn test

echo ""
echo "📊 Generating coverage..."
mvn jacoco:report

echo ""
echo "======================================================================"
echo "✅ SETUP COMPLETE"
echo "======================================================================"
echo ""
echo "Files created:"
echo "  - exception/InvalidPasswordException.java"
echo "  - exception/ResourceNotFoundException.java"
echo "  - entity/PasswordHistory.java"
echo "  - repository/PasswordHistoryRepository.java"
echo "  - Updated: service/security/PasswordPolicyService.java"
echo "  - Updated: service/UserService.java"
echo "  - test/controller/AuthApiTest.java"
echo "  - test/controller/PatientApiTest.java"
echo "  - test/config/OpenAITestConfig.java"
echo "  - test/service/ai/AIRecommendationServiceTest.java"
echo ""
echo "Next steps:"
echo "  1. open target/site/jacoco/index.html"
echo "  2. git add . && git commit -m 'test: Add integration tests and resolve tech debt'"
echo ""