package com.geriatriccare.unit.service;

import com.geriatriccare.dto.MedicationRequest;
import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.repository.MedicationRepository;
import com.geriatriccare.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.geriatriccare.builders.MedicationTestBuilder.aMedication;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationService Tests")
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    private MedicationRequest validRequest;
    private Medication savedMedication;

    @BeforeEach
    void setUp() {
        validRequest = new MedicationRequest();
        validRequest.setName("Aspirin");
        validRequest.setGenericName("Acetylsalicylic Acid");
        validRequest.setDosage("500mg");
        validRequest.setForm(MedicationForm.TABLET);
        validRequest.setManufacturer("Bayer");
        validRequest.setExpirationDate(LocalDate.now().plusYears(1));
        validRequest.setQuantityInStock(100);
        validRequest.setReorderLevel(20);

        savedMedication = aMedication()
                .withName("Aspirin")
                .withGenericName("Acetylsalicylic Acid")
                .withDosage("500mg")
                .tablet()
                .withManufacturer("Bayer")
                .withExpirationDate(LocalDate.now().plusYears(1))
                .withQuantity(100)
                .withReorderLevel(20)
                .build();
    }

    @Nested
    @DisplayName("Create Medication Tests")
    class CreateMedicationTests {

        @Test
        @DisplayName("Should create medication successfully")
        void createMedication_Success() {
            when(medicationRepository.save(any(Medication.class))).thenReturn(savedMedication);
            MedicationResponse response = medicationService.createMedication(validRequest);
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Aspirin");
            assertThat(response.getGenericName()).isEqualTo("Acetylsalicylic Acid");
            verify(medicationRepository).save(any(Medication.class));
        }

        @Test
        @DisplayName("Should throw exception when expiration date is in the past")
        void createMedication_WhenExpired_ThrowsException() {
            validRequest.setExpirationDate(LocalDate.now().minusDays(1));
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Expiration date must be in the future");
        }

        @Test
        @DisplayName("Should throw exception when name is blank")
        void createMedication_WhenNameBlank_ThrowsException() {
            validRequest.setName("   ");
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication name is required");
        }

        @Test
        @DisplayName("Should throw exception when dosage is blank")
        void createMedication_WhenDosageBlank_ThrowsException() {
            validRequest.setDosage("");
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Dosage is required");
        }

        @Test
        @DisplayName("Should throw exception when form is null")
        void createMedication_WhenFormNull_ThrowsException() {
            validRequest.setForm(null);
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication form is required");
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void createMedication_WhenQuantityNegative_ThrowsException() {
            validRequest.setQuantityInStock(-10);
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity cannot be negative");
        }

        @Test
        @DisplayName("Should throw exception when reorder level is negative")
        void createMedication_WhenReorderLevelNegative_ThrowsException() {
            validRequest.setReorderLevel(-5);
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Reorder level cannot be negative");
        }
    }

    @Nested
    @DisplayName("Read Medication Tests")
    class ReadMedicationTests {

        @Test
        @DisplayName("Should get medication by ID")
        void getMedicationById_Success() {
            UUID id = savedMedication.getId();
            when(medicationRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(savedMedication));
            MedicationResponse response = medicationService.getMedicationById(id);
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Aspirin");
            verify(medicationRepository).findByIdAndIsActiveTrue(id);
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void getMedicationById_WhenNotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> medicationService.getMedicationById(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");
        }

        @Test
        @DisplayName("Should get all medications")
        void getAllMedications_Success() {
            Medication med1 = aMedication().withName("Med 1").build();
            Medication med2 = aMedication().withName("Med 2").build();
            when(medicationRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(med1, med2));
            List<MedicationResponse> responses = medicationService.getAllMedications();
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("Med 1");
            assertThat(responses.get(1).getName()).isEqualTo("Med 2");
            verify(medicationRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("Should return empty list when no medications")
        void getAllMedications_WhenEmpty_ReturnsEmptyList() {
            when(medicationRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());
            List<MedicationResponse> responses = medicationService.getAllMedications();
            assertThat(responses).isEmpty();
            verify(medicationRepository).findByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("Update Medication Tests")
    class UpdateMedicationTests {

        @Test
        @DisplayName("Should update medication successfully")
        void updateMedication_Success() {
            UUID id = savedMedication.getId();
            when(medicationRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(savedMedication));
            when(medicationRepository.save(any(Medication.class))).thenReturn(savedMedication);
            validRequest.setName("Updated Aspirin");
            MedicationResponse response = medicationService.updateMedication(id, validRequest);
            assertThat(response).isNotNull();
            verify(medicationRepository).findByIdAndIsActiveTrue(id);
            verify(medicationRepository).save(any(Medication.class));
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void updateMedication_WhenNotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> medicationService.updateMedication(id, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");
        }

        @Test
        @DisplayName("Should throw exception when expiration date is in past")
        void updateMedication_WhenExpiredDate_ThrowsException() {
            UUID id = savedMedication.getId();
            validRequest.setExpirationDate(LocalDate.now().minusDays(1));
            assertThatThrownBy(() -> medicationService.updateMedication(id, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Expiration date must be in the future");
        }
    }

    @Nested
    @DisplayName("Delete Medication Tests")
    class DeleteMedicationTests {

        @Test
        @DisplayName("Should soft delete medication")
        void deleteMedication_Success() {
            UUID id = savedMedication.getId();
            when(medicationRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(savedMedication));
            medicationService.deleteMedication(id);
            verify(medicationRepository).findByIdAndIsActiveTrue(id);
            verify(medicationRepository).save(savedMedication);
            assertThat(savedMedication.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void deleteMedication_WhenNotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> medicationService.deleteMedication(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");
        }
    }

    @Nested
    @DisplayName("Search Medication Tests")
    class SearchMedicationTests {

        @Test
        @DisplayName("Should search medications by name")
        void searchMedicationsByName_Success() {
            Medication aspirin = aMedication().withName("Aspirin").build();
            when(medicationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("aspirin"))
                    .thenReturn(Arrays.asList(aspirin));
            List<MedicationResponse> responses = medicationService.searchByName("aspirin");
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getName()).isEqualTo("Aspirin");
            verify(medicationRepository).findByNameContainingIgnoreCaseAndIsActiveTrue("aspirin");
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void searchMedicationsByName_WhenNoMatches_ReturnsEmpty() {
            when(medicationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("nonexistent"))
                    .thenReturn(Arrays.asList());
            List<MedicationResponse> responses = medicationService.searchByName("nonexistent");
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should search medications by form")
        void searchMedicationsByForm_Success() {
            Medication tablet1 = aMedication().tablet().withName("Med 1").build();
            Medication tablet2 = aMedication().tablet().withName("Med 2").build();
            when(medicationRepository.findByFormAndIsActiveTrue(MedicationForm.TABLET))
                    .thenReturn(Arrays.asList(tablet1, tablet2));
            List<MedicationResponse> responses = medicationService.findByForm(MedicationForm.TABLET);
            assertThat(responses).hasSize(2);
            assertThat(responses).allMatch(r -> r.getForm() == MedicationForm.TABLET);
            verify(medicationRepository).findByFormAndIsActiveTrue(MedicationForm.TABLET);
        }

        @Test
        @DisplayName("Should search medications by manufacturer")
        void searchMedicationsByManufacturer_Success() {
            Medication med = aMedication().withManufacturer("Pfizer").build();
            when(medicationRepository.findByManufacturerIgnoreCaseAndIsActiveTrue("Pfizer"))
                    .thenReturn(Arrays.asList(med));
            List<MedicationResponse> responses = medicationService.findByManufacturer("Pfizer");
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getManufacturer()).isEqualTo("Pfizer");
            verify(medicationRepository).findByManufacturerIgnoreCaseAndIsActiveTrue("Pfizer");
        }
    }

    @Nested
    @DisplayName("Inventory Query Tests")
    class InventoryQueryTests {
        @Test
        @DisplayName("Should get low stock medications")
        void getLowStockMedications_Success() {
            Medication lowStock1 = aMedication().lowStock().withName("Low 1").build();
            Medication lowStock2 = aMedication().lowStock().withName("Low 2").build();
            when(medicationRepository.findLowStockMedications())
                    .thenReturn(Arrays.asList(lowStock1, lowStock2));
            List<MedicationResponse> responses = medicationService.findLowStock();
            assertThat(responses).hasSize(2);
            verify(medicationRepository).findLowStockMedications();
        }

        @Test
        @DisplayName("Should get expired medications")
        void getExpiredMedications_Success() {
            Medication expired1 = aMedication().expired().withName("Expired 1").build();
            Medication expired2 = aMedication().expired().withName("Expired 2").build();
            when(medicationRepository.findExpiredMedications())
                    .thenReturn(Arrays.asList(expired1, expired2));
            List<MedicationResponse> responses = medicationService.findExpired();
            assertThat(responses).hasSize(2);
            verify(medicationRepository).findExpiredMedications();
        }

        @Test
        @DisplayName("Should get expiring soon medications")
        void getExpiringSoonMedications_Success() {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            Medication expiring1 = aMedication().expiringSoon().withName("Expiring 1").build();
            Medication expiring2 = aMedication().expiringSoon().withName("Expiring 2").build();
            when(medicationRepository.findByExpirationDateBeforeAndIsActiveTrue(futureDate))
                    .thenReturn(Arrays.asList(expiring1, expiring2));
            List<MedicationResponse> responses = medicationService.findExpiringSoon(30);
            assertThat(responses).hasSize(2);
            verify(medicationRepository).findByExpirationDateBeforeAndIsActiveTrue(futureDate);
        }

        @Test
        @DisplayName("Should default to 30 days for expiring soon")
        void getExpiringSoonMedications_DefaultDays() {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            when(medicationRepository.findByExpirationDateBeforeAndIsActiveTrue(futureDate))
                    .thenReturn(Arrays.asList());
            List<MedicationResponse> responses = medicationService.findExpiringSoon(30);
            verify(medicationRepository).findByExpirationDateBeforeAndIsActiveTrue(futureDate);
        }
    }
}