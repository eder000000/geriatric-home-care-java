package com.geriatriccare.service.security;

import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.CustomUserDetailsService;
import com.geriatriccare.security.UserPrincipal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomUserDetailsService
 * Tests user loading by username and email
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("johndoe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.PHYSICIAN);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setEmailVerified(true);
        testUser.setMfaEnabled(false);
        testUser.setFailedLoginAttempts(0);
        testUser.setDeleted(false);

        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should load user by username")
    void shouldLoadUserByUsername() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("johndoe");

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals("johndoe", userDetails.getUsername(), "Username should match");
        assertNotNull(userDetails.getPassword(), "Password should not be null");
        assertTrue(userDetails.isEnabled(), "User should be enabled");
        assertTrue(userDetails.isAccountNonLocked(), "Account should not be locked");
        assertTrue(userDetails.isAccountNonExpired(), "Account should not be expired");
        assertTrue(userDetails.isCredentialsNonExpired(), "Credentials should not be expired");

        // Verify authorities/roles
        boolean hasPhysicianRole = userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PHYSICIAN"));
        assertTrue(hasPhysicianRole, "User should have PHYSICIAN role");
    }

    @Test
    @DisplayName("Should load user by email")
    void shouldLoadUserByEmail() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@example.com");

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals("johndoe", userDetails.getUsername(), "Username should match");
        
        // Verify it's the same user
        assertTrue(userDetails instanceof UserPrincipal, "Should be UserPrincipal instance");
        UserPrincipal principal = (UserPrincipal) userDetails;
        assertEquals(testUser.getId(), principal.getId(), "User ID should match");
        assertEquals("john.doe@example.com", principal.getEmail(), "Email should match");
    }

    @Test
    @DisplayName("Should throw exception for non-existent username")
    void shouldThrowExceptionForNonExistentUsername() {
        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent"),
            "Should throw UsernameNotFoundException"
        );

        assertTrue(
            exception.getMessage().contains("nonexistent"),
            "Exception message should contain the username"
        );
    }

    @Test
    @DisplayName("Should throw exception for non-existent email")
    void shouldThrowExceptionForNonExistentEmail() {
        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent@example.com"),
            "Should throw UsernameNotFoundException for non-existent email"
        );
    }

    @Test
    @DisplayName("Should not load inactive user")
    void shouldNotLoadInactiveUser() {
        // Arrange
        testUser.setStatus(UserStatus.INACTIVE);
        userRepository.save(testUser);

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("johndoe"),
            "Should throw exception for inactive user"
        );
    }

    @Test
    @DisplayName("Should not load deleted user")
    void shouldNotLoadDeletedUser() {
        // Arrange
        testUser.setDeleted(true);
        userRepository.save(testUser);

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("johndoe"),
            "Should throw exception for deleted user"
        );
    }

    @Test
    @DisplayName("Should load user by ID")
    void shouldLoadUserById() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserById(testUser.getId().toString());

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertTrue(userDetails instanceof UserPrincipal, "Should be UserPrincipal instance");
        UserPrincipal principal = (UserPrincipal) userDetails;
        assertEquals(testUser.getId(), principal.getId(), "User ID should match");
        assertEquals("johndoe", principal.getUsername(), "Username should match");
    }

    @Test
    @DisplayName("Should throw exception for invalid user ID")
    void shouldThrowExceptionForInvalidUserId() {
        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserById("00000000-0000-0000-0000-000000000000"),
            "Should throw exception for non-existent user ID"
        );
    }

    @Test
    @DisplayName("UserPrincipal should contain all user information")
    void userPrincipalShouldContainAllUserInfo() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("johndoe");

        // Assert
        assertTrue(userDetails instanceof UserPrincipal, "Should be UserPrincipal instance");
        UserPrincipal principal = (UserPrincipal) userDetails;

        assertEquals(testUser.getId(), principal.getId());
        assertEquals(testUser.getUsername(), principal.getUsername());
        assertEquals(testUser.getEmail(), principal.getEmail());
        assertEquals(testUser.getFirstName(), principal.getFirstName());
        assertEquals(testUser.getLastName(), principal.getLastName());
        assertTrue(principal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + testUser.getRole().name())));
        assertNotNull(principal.getAuthorities());
        assertFalse(principal.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Should handle users with special characters in username")
    void shouldHandleSpecialCharactersInUsername() {
        // Arrange
        User specialUser = new User();
        specialUser.setUsername("user.name_123");
        specialUser.setEmail("special@example.com");
        specialUser.setPassword(passwordEncoder.encode("Password123!"));
        specialUser.setFirstName("Special");
        specialUser.setLastName("User");
        specialUser.setRole(UserRole.CAREGIVER);
        specialUser.setStatus(UserStatus.ACTIVE);
        specialUser.setDeleted(false);
        userRepository.save(specialUser);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("user.name_123");

        // Assert
        assertNotNull(userDetails);
        assertEquals("user.name_123", userDetails.getUsername());
    }

    @Test
    @DisplayName("Should handle case-sensitive username lookup")
    void shouldHandleCaseSensitiveUsername() {
        // Act & Assert
        // Assuming username is case-sensitive
        UserDetails userDetails1 = userDetailsService.loadUserByUsername("johndoe");
        assertNotNull(userDetails1);

        // This should fail if username is case-sensitive
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("JohnDoe"),
            "Username lookup should be case-sensitive"
        );
    }
}