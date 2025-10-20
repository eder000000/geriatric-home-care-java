package com.geriatriccare.unit.dto;

import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MedicationResponse DTO Tests")
class MedicationResponseTest {

    @Test
    @DisplayName("Should create response from medication entity")
    void shouldCreateResponseFromEntity() {
        // ARRANGE
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);

        // ACT
        MedicationResponse response = MedicationResponse.fromEntity(medication);

        // ASSERT
        assertThat(response.getId()).isEqualTo(medication.getId());
        assertThat(response.getName()).isEqualTo(medication.getName());
        assertThat(response.getGenericName()).isEqualTo(medication.getGenericName());
        assertThat(response.getDosage()).isEqualTo(medication.getDosage());
        assertThat(response.getForm()).isEqualTo(medication.getForm());
        assertThat(response.getManufacturer()).isEqualTo(medication.getManufacturer());
        assertThat(response.getExpirationDate()).isEqualTo(medication.getExpirationDate());
        assertThat(response.getQuantityInStock()).isEqualTo(medication.getQuantityInStock());
        assertThat(response.getReorderLevel()).isEqualTo(medication.getReorderLevel());
        assertThat(response.getIsActive()).isEqualTo(medication.getIsActive());
        assertThat(response.getCreatedAt()).isEqualTo(medication.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(medication.getUpdatedAt());
    }

    @Test
    @DisplayName("Should include creator name in response")
    void shouldIncludeCreatorName() {
        User creator = createUser("Jane", "Smith");
        Medication medication = createMedication(creator);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getCreatedByName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should include updater name in response")
    void shouldIncludeUpdaterName() {
        User creator = createUser("John", "Doe");
        User updater = createUser("Jane", "Smith");
        Medication medication = createMedication(creator);
        medication.setUpdatedBy(updater);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getUpdatedByName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should calculate isLowStock when quantity at reorder level")
    void shouldCalculateIsLowStockWhenAtReorderLevel() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setQuantityInStock(10);
        medication.setReorderLevel(10);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsLowStock()).isTrue();
    }

    @Test
    @DisplayName("Should calculate isLowStock when quantity below reorder level")
    void shouldCalculateIsLowStockWhenBelowReorderLevel() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setQuantityInStock(5);
        medication.setReorderLevel(10);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsLowStock()).isTrue();
    }

    @Test
    @DisplayName("Should calculate isLowStock false when quantity above reorder level")
    void shouldCalculateIsLowStockFalseWhenAboveReorderLevel() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setQuantityInStock(50);
        medication.setReorderLevel(10);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsLowStock()).isFalse();
    }

    @Test
    @DisplayName("Should calculate isExpired when expiration date in past")
    void shouldCalculateIsExpiredWhenDateInPast() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setExpirationDate(LocalDate.now().minusDays(1));

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsExpired()).isTrue();
    }

    @Test
    @DisplayName("Should calculate isExpired false when expiration date in future")
    void shouldCalculateIsExpiredFalseWhenDateInFuture() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setExpirationDate(LocalDate.now().plusMonths(6));

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsExpired()).isFalse();
    }

    @Test
    @DisplayName("Should calculate isExpiringSoon when expiring in 30 days")
    void shouldCalculateIsExpiringSoonWhenExpiringIn30Days() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setExpirationDate(LocalDate.now().plusDays(15));

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsExpiringSoon()).isTrue();
    }

    @Test
    @DisplayName("Should calculate isExpiringSoon false when expiring after 30 days")
    void shouldCalculateIsExpiringSoonFalseWhenExpiringAfter30Days() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setExpirationDate(LocalDate.now().plusDays(60));

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getIsExpiringSoon()).isFalse();
    }

    @Test
    @DisplayName("Should handle null updatedBy gracefully")
    void shouldHandleNullUpdatedByGracefully() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setUpdatedBy(null);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getUpdatedByName()).isNull();
    }

    @Test
    @DisplayName("Should handle null generic name")
    void shouldHandleNullGenericName() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setGenericName(null);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getGenericName()).isNull();
    }

    @Test
    @DisplayName("Should handle null manufacturer")
    void shouldHandleNullManufacturer() {
        User creator = createUser("John", "Doe");
        Medication medication = createMedication(creator);
        medication.setManufacturer(null);

        MedicationResponse response = MedicationResponse.fromEntity(medication);

        assertThat(response.getManufacturer()).isNull();
    }

    // Helper methods
    private User createUser(String firstName, String lastName) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "@test.com");
        return user;
    }

    private Medication createMedication(User creator) {
        Medication medication = new Medication();
        medication.setId(UUID.randomUUID());
        medication.setName("Aspirin");
        medication.setGenericName("Acetylsalicylic Acid");
        medication.setDosage("500mg");
        medication.setForm(MedicationForm.TABLET);
        medication.setManufacturer("Bayer");
        medication.setExpirationDate(LocalDate.now().plusYears(1));
        medication.setQuantityInStock(100);
        medication.setReorderLevel(20);
        medication.setCreatedAt(LocalDateTime.now());
        medication.setUpdatedAt(LocalDateTime.now());
        medication.setCreatedBy(creator);
        medication.setUpdatedBy(creator);
        return medication;
    }
}