package com.geriatriccare.base;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;

class BaseIntegrationVerificationTest extends BaseIntegrationTest {
    
    @Test
    void testUsersCreated() {
        assertThat(ownerUser).isNotNull();
        assertThat(ownerUser.getEmail()).isEqualTo("owner@test.com");
        
        assertThat(caregiverUser).isNotNull();
        assertThat(caregiverUser.getEmail()).isEqualTo("caregiver@test.com");
    }
    
    @Test
    void testTokensObtained() {
        assertThat(ownerToken).isNotNull();
        assertThat(ownerToken).isNotEmpty();
        
        assertThat(caregiverToken).isNotNull();
        assertThat(caregiverToken).isNotEmpty();
    }
    
    @Test
    void testAuthenticatedRequest() {
        ResponseEntity<Object> response = getWithAuth("/api/users/profile", ownerToken, Object.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}