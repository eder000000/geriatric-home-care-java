package com.geriatriccare.unit.service;

import com.geriatriccare.dto.InteractionCheckRequest;
import com.geriatriccare.dto.InteractionCheckResponse;
import com.geriatriccare.entity.DrugInteraction;
import com.geriatriccare.entity.InteractionSeverity;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.repository.DrugInteractionRepository;
import com.geriatriccare.repository.MedicationRepository;
import com.geriatriccare.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.geriatriccare.builders.MedicationTestBuilder.aMedication;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Drug Interaction Service Tests")
class DrugInteractionServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private DrugInteractionRepository drugInteractionRepository;

    @InjectMocks
    private MedicationService medicationService;

    private DrugInteraction minorInteraction;
    private DrugInteraction moderateInteraction;
    private DrugInteraction severeInteraction;

    @BeforeEach
    void setUp() {
        minorInteraction = new DrugInteraction();
        minorInteraction.setMedication1Name("Aspirin");
        minorInteraction.setMedication2Name("Ibuprofen");
        minorInteraction.setSeverity(InteractionSeverity.MINOR);
        minorInteraction.setDescription("Increased bleeding risk");
        minorInteraction.setRecommendation("Monitor for bleeding");

        moderateInteraction = new DrugInteraction();
        moderateInteraction.setMedication1Name("Warfarin");
        moderateInteraction.setMedication2Name("Aspirin");
        moderateInteraction.setSeverity(InteractionSeverity.MODERATE);
        moderateInteraction.setDescription("Significant bleeding risk");
        moderateInteraction.setRecommendation("Use with caution");

        severeInteraction = new DrugInteraction();
        severeInteraction.setMedication1Name("MAO Inhibitor");
        severeInteraction.setMedication2Name("SSRI");
        severeInteraction.setSeverity(InteractionSeverity.SEVERE);
        severeInteraction.setDescription("Serotonin syndrome risk");
        severeInteraction.setRecommendation("Avoid combination");
    }

    @Test
    @DisplayName("Should detect interaction between two medications")
    void checkInteraction_TwoMeds_DetectsInteraction() {
        when(drugInteractionRepository.findInteraction("Aspirin", "Ibuprofen"))
                .thenReturn(Optional.of(minorInteraction));

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Aspirin", "Ibuprofen"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.isHasInteractions()).isTrue();
        assertThat(response.getInteractionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return no interactions when safe")
    void checkInteraction_SafeCombination_NoInteractions() {
        when(drugInteractionRepository.findInteraction(any(), any()))
                .thenReturn(Optional.empty());

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Med A", "Med B"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.isHasInteractions()).isFalse();
    }

    @Test
    @DisplayName("Should handle empty medication list")
    void checkInteraction_EmptyList_NoInteractions() {
        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Collections.emptyList());

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.isHasInteractions()).isFalse();
    }

    @Test
    @DisplayName("Should handle single medication")
    void checkInteraction_SingleMed_NoInteractions() {
        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Aspirin"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.isHasInteractions()).isFalse();
    }

    @Test
    @DisplayName("Should classify minor interaction correctly")
    void classifyInteraction_Minor() {
        when(drugInteractionRepository.findInteraction("Aspirin", "Ibuprofen"))
                .thenReturn(Optional.of(minorInteraction));

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Aspirin", "Ibuprofen"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.getInteractions().get(0).getSeverity())
                .isEqualTo(InteractionSeverity.MINOR);
    }

    @Test
    @DisplayName("Should classify moderate interaction correctly")
    void classifyInteraction_Moderate() {
        when(drugInteractionRepository.findInteraction("Warfarin", "Aspirin"))
                .thenReturn(Optional.of(moderateInteraction));

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Warfarin", "Aspirin"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.getInteractions().get(0).getSeverity())
                .isEqualTo(InteractionSeverity.MODERATE);
    }

    @Test
    @DisplayName("Should classify severe interaction correctly")
    void classifyInteraction_Severe() {
        when(drugInteractionRepository.findInteraction("MAO Inhibitor", "SSRI"))
                .thenReturn(Optional.of(severeInteraction));

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("MAO Inhibitor", "SSRI"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.getInteractions().get(0).getSeverity())
                .isEqualTo(InteractionSeverity.SEVERE);
    }

    @Test
    @DisplayName("Should check interactions by medication IDs")
    void reviewPatientMedications_ByIds_Success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Medication med1 = aMedication().withName("Aspirin").build();
        med1.setId(id1);
        Medication med2 = aMedication().withName("Warfarin").build();
        med2.setId(id2);

        when(medicationRepository.findByIdAndIsActiveTrue(id1))
                .thenReturn(Optional.of(med1));
        when(medicationRepository.findByIdAndIsActiveTrue(id2))
                .thenReturn(Optional.of(med2));
        when(drugInteractionRepository.findInteraction("Aspirin", "Warfarin"))
                .thenReturn(Optional.of(moderateInteraction));

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationIds(Arrays.asList(id1, id2));

        InteractionCheckResponse response = medicationService.checkDrugInteractionsByIds(request);

        assertThat(response.isHasInteractions()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for null medication names")
    void checkInteraction_NullNames_ThrowsException() {
        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(null);

        assertThatThrownBy(() -> medicationService.checkDrugInteractions(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should handle bidirectional interaction lookup")
    void checkInteraction_Bidirectional_Success() {
        when(drugInteractionRepository.findInteraction("Ibuprofen", "Aspirin"))
                .thenReturn(Optional.of(minorInteraction));

        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Ibuprofen", "Aspirin"));

        InteractionCheckResponse response = medicationService.checkDrugInteractions(request);

        assertThat(response.isHasInteractions()).isTrue();
    }

    @Test
    @DisplayName("Should skip checking medication against itself")
    void checkInteraction_SameMed_SkipsCheck() {
        InteractionCheckRequest request = new InteractionCheckRequest();
        request.setMedicationNames(Arrays.asList("Aspirin", "Aspirin"));

        medicationService.checkDrugInteractions(request);

        verify(drugInteractionRepository, never()).findInteraction("Aspirin", "Aspirin");
    }
}
