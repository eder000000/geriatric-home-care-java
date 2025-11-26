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

    @Test
    @DisplayName("Should calculate weight-based dosage")
    void calculateWeightBasedDosage_Success() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setPatientWeight(70.0);
        request.setDosagePerKg(1.0);

        DosageCalculationResponse response = medicationService.calculateWeightBasedDosage(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("Should calculate age-based dosage for pediatric")
    void calculateAgeBasedDosage_Pediatric() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setPatientAge(10);
        request.setBaseDosage(100.0);

        DosageCalculationResponse response = medicationService.calculateAgeBasedDosage(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(50.0);
        assertThat(response.getAgeGroup()).isEqualTo("PEDIATRIC");
    }

    @Test
    @DisplayName("Should calculate age-based dosage for adult")
    void calculateAgeBasedDosage_Adult() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setPatientAge(30);
        request.setBaseDosage(100.0);

        DosageCalculationResponse response = medicationService.calculateAgeBasedDosage(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(100.0);
        assertThat(response.getAgeGroup()).isEqualTo("ADULT");
    }

    @Test
    @DisplayName("Should calculate age-based dosage for geriatric")
    void calculateAgeBasedDosage_Geriatric() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setPatientAge(70);
        request.setBaseDosage(100.0);

        DosageCalculationResponse response = medicationService.calculateAgeBasedDosage(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(75.0);
        assertThat(response.getAgeGroup()).isEqualTo("GERIATRIC");
    }

    @Test
    @DisplayName("Should validate max daily dose within limit")
    void validateMaximumDailyDose_WithinLimit() {
        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setCalculatedDosage(100.0);
        request.setMaxDailyDose(400.0);
        request.setFrequency(3);

        boolean isValid = medicationService.validateMaximumDailyDose(request);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject dosage exceeding max daily limit")
    void validateMaximumDailyDose_ExceedsLimit() {
        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setCalculatedDosage(150.0);
        request.setMaxDailyDose(400.0);
        request.setFrequency(3);

        boolean isValid = medicationService.validateMaximumDailyDose(request);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should adjust for normal renal function")
    void adjustDosageForRenalFunction_Normal() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setBaseDosage(100.0);
        request.setGlomerularFiltrationRate(90.0);

        DosageCalculationResponse response = medicationService.adjustDosageForRenalFunction(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should adjust for severe renal impairment")
    void adjustDosageForRenalFunction_Severe() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setBaseDosage(100.0);
        request.setGlomerularFiltrationRate(25.0);

        DosageCalculationResponse response = medicationService.adjustDosageForRenalFunction(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(50.0);
        assertThat(response.getRenalAdjustmentApplied()).isTrue();
    }

    @Test
    @DisplayName("Should adjust for hepatic impairment Grade B")
    void adjustDosageForHepaticFunction_GradeB() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setBaseDosage(100.0);
        request.setChildPughScore("B");

        DosageCalculationResponse response = medicationService.adjustDosageForHepaticFunction(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(75.0);
        assertThat(response.getHepaticAdjustmentApplied()).isTrue();
    }

    @Test
    @DisplayName("Should calculate complex dosage with multiple factors")
    void calculateComplexDosage_MultipleFactors() {
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        DosageCalculationRequest request = new DosageCalculationRequest();
        request.setMedicationId(medicationId);
        request.setPatientAge(70);
        request.setPatientWeight(60.0);
        request.setDosagePerKg(1.0);
        request.setGlomerularFiltrationRate(25.0);

        DosageCalculationResponse response = medicationService.calculateComplexDosage(request);

        assertThat(response.getCalculatedDosage()).isEqualTo(22.5);
        assertThat(response.getWarnings()).isNotEmpty();
    }
}
