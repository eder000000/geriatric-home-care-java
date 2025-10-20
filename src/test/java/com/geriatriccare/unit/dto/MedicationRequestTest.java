package com.geriatriccare.unit.dto;
    
    import com.geriatriccare.dto.MedicationRequest;
    import com.geriatriccare.entity.MedicationForm;
    import jakarta.validation.ConstraintViolation;
    import jakarta.validation.Validation;
    import jakarta.validation.Validator;
    import jakarta.validation.ValidatorFactory;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;
    
    import java.time.LocalDate;
    import java.util.Set;
    
    import static org.assertj.core.api.Assertions.assertThat;
    
    @DisplayName("MedicationRequest DTO Tests")
    class MedicationRequestTest {
    
        private Validator validator;
    
        @BeforeEach
        void setUp() {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        }
    
        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            MedicationRequest request = new MedicationRequest();
            request.setName("Aspirin");
            request.setGenericName("Acetylsalicylic Acid");
            request.setDosage("500mg");
            request.setForm(MedicationForm.TABLET);
            request.setManufacturer("Bayer");
            request.setExpirationDate(LocalDate.now().plusYears(1));
            request.setQuantityInStock(100);
            request.setReorderLevel(20);
    
            assertThat(request.getName()).isEqualTo("Aspirin");
            assertThat(request.getGenericName()).isEqualTo("Acetylsalicylic Acid");
            assertThat(request.getDosage()).isEqualTo("500mg");
            assertThat(request.getForm()).isEqualTo(MedicationForm.TABLET);
            assertThat(request.getManufacturer()).isEqualTo("Bayer");
            assertThat(request.getExpirationDate()).isAfter(LocalDate.now());
            assertThat(request.getQuantityInStock()).isEqualTo(100);
            assertThat(request.getReorderLevel()).isEqualTo(20);
        }
    
        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailValidationWhenNameIsBlank() {
            MedicationRequest request = createValidRequest();
            request.setName("");
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }
    
        @Test
        @DisplayName("Should fail validation when name is null")
        void shouldFailValidationWhenNameIsNull() {
            MedicationRequest request = createValidRequest();
            request.setName(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when name exceeds 255 characters")
        void shouldFailValidationWhenNameTooLong() {
            MedicationRequest request = createValidRequest();
            request.setName("A".repeat(256));
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when dosage is blank")
        void shouldFailValidationWhenDosageIsBlank() {
            MedicationRequest request = createValidRequest();
            request.setDosage("");
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when form is null")
        void shouldFailValidationWhenFormIsNull() {
            MedicationRequest request = createValidRequest();
            request.setForm(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when expirationDate is null")
        void shouldFailValidationWhenExpirationDateIsNull() {
            MedicationRequest request = createValidRequest();
            request.setExpirationDate(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when expirationDate is in past")
        void shouldFailValidationWhenExpirationDateInPast() {
            MedicationRequest request = createValidRequest();
            request.setExpirationDate(LocalDate.now().minusDays(1));
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when quantityInStock is null")
        void shouldFailValidationWhenQuantityIsNull() {
            MedicationRequest request = createValidRequest();
            request.setQuantityInStock(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when quantityInStock is negative")
        void shouldFailValidationWhenQuantityIsNegative() {
            MedicationRequest request = createValidRequest();
            request.setQuantityInStock(-1);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when reorderLevel is null")
        void shouldFailValidationWhenReorderLevelIsNull() {
            MedicationRequest request = createValidRequest();
            request.setReorderLevel(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should fail validation when reorderLevel is negative")
        void shouldFailValidationWhenReorderLevelIsNegative() {
            MedicationRequest request = createValidRequest();
            request.setReorderLevel(-1);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isNotEmpty();
        }
    
        @Test
        @DisplayName("Should pass validation with valid data")
        void shouldPassValidationWithValidData() {
            MedicationRequest request = createValidRequest();
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isEmpty();
        }
    
        @Test
        @DisplayName("Generic name should be optional")
        void genericNameShouldBeOptional() {
            MedicationRequest request = createValidRequest();
            request.setGenericName(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isEmpty();
        }
    
        @Test
        @DisplayName("Manufacturer should be optional")
        void manufacturerShouldBeOptional() {
            MedicationRequest request = createValidRequest();
            request.setManufacturer(null);
    
            Set<ConstraintViolation<MedicationRequest>> violations = validator.validate(request);
    
            assertThat(violations).isEmpty();
        }
    
        private MedicationRequest createValidRequest() {
            MedicationRequest request = new MedicationRequest();
            request.setName("Test Medication");
            request.setDosage("100mg");
            request.setForm(MedicationForm.TABLET);
            request.setExpirationDate(LocalDate.now().plusYears(1));
            request.setQuantityInStock(50);
            request.setReorderLevel(10);
            return request;
        }
}