package com.geriatriccare.unit.service;

import com.geriatriccare.dto.DosageCalculationRequest;
import com.geriatriccare.dto.DosageCalculationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.repository.DrugInteractionRepository;
import com.geriatriccare.repository.MedicationRepository;
import com.geriatriccare.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.geriatriccare.builders.MedicationTestBuilder.aMedication;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Medication Dosage Service Tests")
class MedicationDosageServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private DrugInteractionRepository drugInteractionRepository;

    @InjectMocks
    private MedicationService medicationService;

    private UUID medicationId;
    private Medication testMedication;

    @BeforeEach
    void setUp() {
        medicationId = UUID.randomUUID();
        testMedication = aMedication().withName("Test Med").build();
        testMedication.setId(medicationId);
    }

    @Nested
    @DisplayName("Weight-Based Dosage Tests")
    class WeightBasedDosageTests {

        @ParameterizedTest(name = "weight={0}kg, dosagePerKg={1}, expected={2}mg")
        @CsvSource({
            "50, 1.0, 50",
            "70, 1.0, 70",
            "100, 1.0, 100",
            "30, 2.0, 60",
            "80, 0.5, 40"
        })
        @DisplayName("Should calculate dosage based on weight")
        void calculateWeightBasedDosage_VariousWeights(double weight, double dosagePerKg, double expected) {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientWeight(weight);
            request.setDosagePerKg(dosagePerKg);

            DosageCalculationResponse response = medicationService.calculateWeightBasedDosage(request);

            assertThat(response.getCalculatedDosage()).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should throw exception for negative weight")
        void calculateWeightBasedDosage_NegativeWeight_ThrowsException() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientWeight(-10.0);
            request.setDosagePerKg(1.0);

            assertThatThrownBy(() -> medicationService.calculateWeightBasedDosage(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Weight must be positive");
        }

        @Test
        @DisplayName("Should throw exception for zero weight")
        void calculateWeightBasedDosage_ZeroWeight_ThrowsException() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientWeight(0.0);
            request.setDosagePerKg(1.0);

            assertThatThrownBy(() -> medicationService.calculateWeightBasedDosage(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Weight must be positive");
        }
    }

    @Nested
    @DisplayName("Age-Based Dosage Tests")
    class AgeBasedDosageTests {

        @ParameterizedTest(name = "age={0}, baseDosage={1}, expected={2}")
        @CsvSource({
            "5, 100, 50",      // Pediatric
            "17, 100, 50",     // Pediatric boundary
            "18, 100, 100",    // Adult
            "30, 100, 100",    // Adult
            "64, 100, 100",    // Adult boundary
            "65, 100, 75",     // Geriatric
            "80, 100, 75"      // Geriatric
        })
        @DisplayName("Should adjust dosage based on age")
        void calculateAgeBasedDosage_VariousAges(int age, double baseDosage, double expected) {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientAge(age);
            request.setBaseDosage(baseDosage);

            DosageCalculationResponse response = medicationService.calculateAgeBasedDosage(request);

            assertThat(response.getCalculatedDosage()).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should set correct age group for pediatric")
        void calculateAgeBasedDosage_Pediatric_SetsAgeGroup() {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientAge(10);
            request.setBaseDosage(100.0);

            DosageCalculationResponse response = medicationService.calculateAgeBasedDosage(request);

            assertThat(response.getAgeGroup()).isEqualTo("PEDIATRIC");
        }

        @Test
        @DisplayName("Should throw exception for negative age")
        void calculateAgeBasedDosage_NegativeAge_ThrowsException() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientAge(-5);
            request.setBaseDosage(100.0);

            assertThatThrownBy(() -> medicationService.calculateAgeBasedDosage(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Age must be positive");
        }
    }

    @Nested
    @DisplayName("Maximum Daily Dose Tests")
    class MaximumDailyDoseTests {

        @Test
        @DisplayName("Should validate dosage within limit")
        void validateMaximumDailyDose_WithinLimit_ReturnsTrue() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setCalculatedDosage(100.0);
            request.setMaxDailyDose(400.0);
            request.setFrequency(3);

            boolean isValid = medicationService.validateMaximumDailyDose(request);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject dosage exceeding limit")
        void validateMaximumDailyDose_ExceedsLimit_ReturnsFalse() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setCalculatedDosage(150.0);
            request.setMaxDailyDose(400.0);
            request.setFrequency(3);

            boolean isValid = medicationService.validateMaximumDailyDose(request);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should calculate total daily dose")
        void calculateTotalDailyDose_Success() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setCalculatedDosage(100.0);
            request.setFrequency(4);

            double total = medicationService.calculateTotalDailyDose(request);

            assertThat(total).isEqualTo(400.0);
        }
    }

    @Nested
    @DisplayName("Renal Function Adjustment Tests")
    class RenalFunctionAdjustmentTests {

        @ParameterizedTest(name = "GFR={0}, baseDosage={1}, expected={2}")
        @CsvSource({
            "90, 100, 100",   // Normal
            "60, 100, 100",   // Mild
            "45, 100, 75",    // Moderate
            "25, 100, 50",    // Severe
            "10, 100, 25"     // End-stage
        })
        @DisplayName("Should adjust dosage based on GFR")
        void adjustDosageForRenalFunction_VariousGFR(double gfr, double baseDosage, double expected) {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setBaseDosage(baseDosage);
            request.setGlomerularFiltrationRate(gfr);

            DosageCalculationResponse response = medicationService.adjustDosageForRenalFunction(request);

            assertThat(response.getCalculatedDosage()).isEqualTo(expected);
            assertThat(response.getRenalAdjustmentApplied()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for negative GFR")
        void adjustDosageForRenalFunction_NegativeGFR_ThrowsException() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setBaseDosage(100.0);
            request.setGlomerularFiltrationRate(-10.0);

            assertThatThrownBy(() -> medicationService.adjustDosageForRenalFunction(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("GFR must be positive");
        }
    }

    @Nested
    @DisplayName("Hepatic Function Adjustment Tests")
    class HepaticFunctionAdjustmentTests {

        @ParameterizedTest(name = "Child-Pugh={0}, baseDosage={1}, expected={2}")
        @CsvSource({
            "A, 100, 100",
            "B, 100, 75",
            "C, 100, 50"
        })
        @DisplayName("Should adjust dosage based on Child-Pugh score")
        void adjustDosageForHepaticFunction_VariousScores(String score, double baseDosage, double expected) {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setBaseDosage(baseDosage);
            request.setChildPughScore(score);

            DosageCalculationResponse response = medicationService.adjustDosageForHepaticFunction(request);

            assertThat(response.getCalculatedDosage()).isEqualTo(expected);
            assertThat(response.getHepaticAdjustmentApplied()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for invalid score")
        void adjustDosageForHepaticFunction_InvalidScore_ThrowsException() {
            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setBaseDosage(100.0);
            request.setChildPughScore("X");

            assertThatThrownBy(() -> medicationService.adjustDosageForHepaticFunction(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid Child-Pugh score");
        }
    }

    @Nested
    @DisplayName("Complex Dosage Calculation Tests")
    class ComplexDosageCalculationTests {

        @Test
        @DisplayName("Should combine age and weight adjustments")
        void calculateComplexDosage_AgeAndWeight() {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientAge(10);
            request.setPatientWeight(30.0);
            request.setDosagePerKg(2.0);

            DosageCalculationResponse response = medicationService.calculateComplexDosage(request);

            assertThat(response.getCalculatedDosage()).isEqualTo(30.0); // 60 * 0.5
        }

        @Test
        @DisplayName("Should combine all adjustment factors")
        void calculateComplexDosage_AllFactors() {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientAge(70);
            request.setPatientWeight(60.0);
            request.setDosagePerKg(1.0);
            request.setGlomerularFiltrationRate(25.0);

            DosageCalculationResponse response = medicationService.calculateComplexDosage(request);

            assertThat(response.getCalculatedDosage()).isEqualTo(22.5); // 60 * 0.75 * 0.5
            assertThat(response.getWarnings()).isNotEmpty();
        }

        @Test
        @DisplayName("Should include warnings for geriatric patients")
        void calculateComplexDosage_Geriatric_IncludesWarnings() {
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            DosageCalculationRequest request = new DosageCalculationRequest();
            request.setMedicationId(medicationId);
            request.setPatientAge(75);
            request.setPatientWeight(70.0);
            request.setDosagePerKg(1.0);

            DosageCalculationResponse response = medicationService.calculateComplexDosage(request);

            assertThat(response.getWarnings()).contains("Geriatric patient - monitor closely");
        }
    }
}