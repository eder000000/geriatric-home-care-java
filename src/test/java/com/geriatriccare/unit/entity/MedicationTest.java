package com.geriatriccare.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("Medication Entity Tests")
public class MedicationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }   

        // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        Medication medication = createValidMedication();
        medication.setName("");

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        Medication medication = createValidMedication();
        medication.setName(null);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void shouldFailValidationWhenNameTooLong() {
        Medication medication = createValidMedication();
        medication.setName("A".repeat(256));

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Should fail validation when dosage is blank")
    void shouldFailValidationWhenDosageIsBlank() {
        Medication medication = createValidMedication();
        medication.setDosage("");

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dosage"));
    }

    @Test
    @DisplayName("Should fail validation when dosage is null")
    void shouldFailValidationWhenDosageIsNull() {
        Medication medication = createValidMedication();
        medication.setDosage(null);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dosage"));
    }

    @Test
    @DisplayName("Should fail validation when form is null")
    void shouldFailValidationWhenFormIsNull() {
        Medication medication = createValidMedication();
        medication.setForm(null);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("form"));
    }

    @Test
    @DisplayName("Should fail validation when expirationDate is null")
    void shouldFailValidationWhenExpirationDateIsNull() {
        Medication medication = createValidMedication();
        medication.setExpirationDate(null);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("expirationDate"));
    }

    @Test
    @DisplayName("Should fail validation when expirationDate is in the past")
    void shouldFailValidationWhenExpirationDateInPast() {
        Medication medication = createValidMedication();
        medication.setExpirationDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("expirationDate"));
    }

    @Test
    @DisplayName("Should fail validation when quantityInStock is null")
    void shouldFailValidationWhenQuantityIsNull() {
        Medication medication = createValidMedication();
        medication.setQuantityInStock(null);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantityInStock"));
    }

    @Test
    @DisplayName("Should fail validation when quantityInStock is negative")
    void shouldFailValidationWhenQuantityIsNegative() {
        Medication medication = createValidMedication();
        medication.setQuantityInStock(-1);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantityInStock"));
    }

    @Test
    @DisplayName("Should fail validation when reorderLevel is null")
    void shouldFailValidationWhenReorderLevelIsNull() {
        Medication medication = createValidMedication();
        medication.setReorderLevel(null);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("reorderLevel"));
    }

    @Test
    @DisplayName("Should fail validation when reorderLevel is negative")
    void shouldFailValidationWhenReorderLevelIsNegative() {
        Medication medication = createValidMedication();
        medication.setReorderLevel(-1);

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("reorderLevel"));
    }

    @Test
    @DisplayName("Should pass validation with all valid fields")
    void shouldPassValidationWithValidFields() {
        Medication medication = createValidMedication();

        Set<ConstraintViolation<Medication>> violations = validator.validate(medication);

        assertThat(violations).isEmpty();
    }

    // ========== AUDIT FIELDS TESTS ==========

    @Test
    @DisplayName("Should have createdAt field")
    void shouldHaveCreatedAtField() {
        Medication medication = new Medication();
        LocalDateTime now = LocalDateTime.now();
        medication.setCreatedAt(now);

        assertThat(medication.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should have updatedAt field")
    void shouldHaveUpdatedAtField() {
        Medication medication = new Medication();
        LocalDateTime now = LocalDateTime.now();
        medication.setUpdatedAt(now);

        assertThat(medication.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should have createdBy user relationship")
    void shouldHaveCreatedByField() {
        Medication medication = new Medication();
        User user = new User();
        user.setId(UUID.randomUUID());
        medication.setCreatedBy(user);

        assertThat(medication.getCreatedBy()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should have updatedBy user relationship")
    void shouldHaveUpdatedByField() {
        Medication medication = new Medication();
        User user = new User();
        user.setId(UUID.randomUUID());
        medication.setUpdatedBy(user);

        assertThat(medication.getUpdatedBy()).isEqualTo(user);
    }

    // ========== HELPER METHOD ==========

    private Medication createValidMedication() {
        Medication medication = new Medication();
        medication.setName("Test Medication");
        medication.setDosage("100mg");
        medication.setForm(MedicationForm.TABLET);
        medication.setExpirationDate(LocalDate.now().plusYears(1));
        medication.setQuantityInStock(50);
        medication.setReorderLevel(10);
        return medication;
    }
}
