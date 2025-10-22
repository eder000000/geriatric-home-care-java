package com.geriatriccare.unit.service;

import com.geriatriccare.dto.MedicationRequest;
import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.MedicationRepository;
import com.geriatriccare.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MedicationService medicationService;

    private User testUser;
    private MedicationRequest validRequest;
    private Medication savedMedication;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test@example.com");

        // Setup valid request
        validRequest = new MedicationRequest();
        validRequest.setName("Aspirin");
        validRequest.setGenericName("Acetylsalicylic Acid");
        validRequest.setDosage("500mg");
        validRequest.setForm(MedicationForm.TABLET);
        validRequest.setManufacturer("Bayer");
        validRequest.setExpirationDate(LocalDate.now().plusYears(1));
        validRequest.setQuantityInStock(100);
        validRequest.setReorderLevel(20);

        // Setup saved medication
        savedMedication = aMedication()
                .withName("Aspirin")
                .withCreatedBy(testUser)
                .build();
    }

    // ========================================
    // CREATE TESTS
    // ========================================

    @Nested
    @DisplayName("Create Medication Tests")
    class CreateMedicationTests {

        @Test
        @DisplayName("Should create medication successfully")
        void createMedication_Success() {
            // ARRANGE
            when(medicationRepository.save(any(Medication.class))).thenReturn(savedMedication);

            // ACT
            MedicationResponse response = medicationService.createMedication(validRequest);

            // ASSERT
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Aspirin");
            verify(medicationRepository, times(1)).save(any(Medication.class));
        }

        @Test
        @DisplayName("Should set audit fields on create")
        void createMedication_ShouldSetAuditFields() {
            // ARRANGE
            ArgumentCaptor<Medication> medicationCaptor = ArgumentCaptor.forClass(Medication.class);
            when(medicationRepository.save(any(Medication.class))).thenReturn(savedMedication);

            // ACT
            medicationService.createMedication(validRequest);

            // ASSERT
            verify(medicationRepository).save(medicationCaptor.capture());
            Medication captured = medicationCaptor.getValue();
            
            assertThat(captured.getCreatedAt()).isNotNull();
            assertThat(captured.getUpdatedAt()).isNotNull();
            assertThat(captured.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when expiration date is in the past")
        void createMedication_WhenExpired_ThrowsException() {
            // ARRANGE
            validRequest.setExpirationDate(LocalDate.now().minusDays(1));

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Expiration date must be in the future");

            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void createMedication_WhenNegativeQuantity_ThrowsException() {
            // ARRANGE
            validRequest.setQuantityInStock(-10);

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity cannot be negative");

            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when reorder level is negative")
        void createMedication_WhenNegativeReorderLevel_ThrowsException() {
            // ARRANGE
            validRequest.setReorderLevel(-5);

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Reorder level cannot be negative");

            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when name is blank after trim")
        void createMedication_WhenBlankName_ThrowsException() {
            // ARRANGE
            validRequest.setName("   ");

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.createMedication(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication name is required");

            verify(medicationRepository, never()).save(any());
        }
    }

    // ========================================
    // READ TESTS
    // ========================================

    @Nested
    @DisplayName("Get Medication Tests")
    class GetMedicationTests {

        @Test
        @DisplayName("Should get medication by ID")
        void getMedicationById_Success() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.of(savedMedication));

            // ACT
            MedicationResponse response = medicationService.getMedicationById(id);

            // ASSERT
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Aspirin");
            verify(medicationRepository).findByIdAndIsActiveTrue(id);
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void getMedicationById_WhenNotFound_ThrowsException() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.getMedicationById(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");

            verify(medicationRepository).findByIdAndIsActiveTrue(id);
        }

        @Test
        @DisplayName("Should get all active medications")
        void getAllMedications_Success() {
            // ARRANGE
            Medication med1 = aMedication().withName("Med 1").build();
            Medication med2 = aMedication().withName("Med 2").build();
            when(medicationRepository.findByIsActiveTrue())
                    .thenReturn(Arrays.asList(med1, med2));

            // ACT
            List<MedicationResponse> responses = medicationService.getAllMedications();

            // ASSERT
            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(MedicationResponse::getName)
                    .containsExactly("Med 1", "Med 2");
            verify(medicationRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("Should return empty list when no active medications")
        void getAllMedications_WhenEmpty_ReturnsEmptyList() {
            // ARRANGE
            when(medicationRepository.findByIsActiveTrue())
                    .thenReturn(Arrays.asList());

            // ACT
            List<MedicationResponse> responses = medicationService.getAllMedications();

            // ASSERT
            assertThat(responses).isEmpty();
            verify(medicationRepository).findByIsActiveTrue();
        }
    }

    // ========================================
    // SEARCH TESTS
    // ========================================

    @Nested
    @DisplayName("Search Medication Tests")
    class SearchMedicationTests {

        @Test
        @DisplayName("Should search medications by name")
        void searchByName_Success() {
            // ARRANGE
            Medication aspirin = aMedication().withName("Aspirin").build();
            when(medicationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("asp"))
                    .thenReturn(Arrays.asList(aspirin));

            // ACT
            List<MedicationResponse> responses = medicationService.searchByName("asp");

            // ASSERT
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getName()).isEqualTo("Aspirin");
            verify(medicationRepository).findByNameContainingIgnoreCaseAndIsActiveTrue("asp");
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void searchByName_WhenNoMatches_ReturnsEmptyList() {
            // ARRANGE
            when(medicationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("xyz"))
                    .thenReturn(Arrays.asList());

            // ACT
            List<MedicationResponse> responses = medicationService.searchByName("xyz");

            // ASSERT
            assertThat(responses).isEmpty();
            verify(medicationRepository).findByNameContainingIgnoreCaseAndIsActiveTrue("xyz");
        }

        @Test
        @DisplayName("Should search by medication form")
        void searchByForm_Success() {
            // ARRANGE
            Medication tablet1 = aMedication().tablet().withName("Med 1").build();
            Medication tablet2 = aMedication().tablet().withName("Med 2").build();
            when(medicationRepository.findByFormAndIsActiveTrue(MedicationForm.TABLET))
                    .thenReturn(Arrays.asList(tablet1, tablet2));

            // ACT
            List<MedicationResponse> responses = medicationService.searchByForm(MedicationForm.TABLET);

            // ASSERT
            assertThat(responses).hasSize(2);
            verify(medicationRepository).findByFormAndIsActiveTrue(MedicationForm.TABLET);
        }

        @Test
        @DisplayName("Should search by manufacturer")
        void searchByManufacturer_Success() {
            // ARRANGE
            Medication med = aMedication().withManufacturer("Pfizer").build();
            when(medicationRepository.findByManufacturerIgnoreCaseAndIsActiveTrue("pfizer"))
                    .thenReturn(Arrays.asList(med));

            // ACT
            List<MedicationResponse> responses = medicationService.searchByManufacturer("pfizer");

            // ASSERT
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getManufacturer()).isEqualTo("Pfizer");
            verify(medicationRepository).findByManufacturerIgnoreCaseAndIsActiveTrue("pfizer");
        }
    }

    // ========================================
    // UPDATE TESTS
    // ========================================

    @Nested
    @DisplayName("Update Medication Tests")
    class UpdateMedicationTests {

        @Test
        @DisplayName("Should update medication successfully")
        void updateMedication_Success() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            Medication existing = aMedication().withName("Old Name").build();
            existing.setId(id);
            
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.of(existing));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(existing);

            MedicationRequest updateRequest = new MedicationRequest();
            updateRequest.setName("New Name");
            updateRequest.setDosage("250mg");
            updateRequest.setForm(MedicationForm.CAPSULE);
            updateRequest.setExpirationDate(LocalDate.now().plusYears(2));
            updateRequest.setQuantityInStock(50);
            updateRequest.setReorderLevel(10);

            // ACT
            MedicationResponse response = medicationService.updateMedication(id, updateRequest);

            // ASSERT
            assertThat(response).isNotNull();
            verify(medicationRepository).findByIdAndIsActiveTrue(id);
            verify(medicationRepository).save(any(Medication.class));
        }

        @Test
        @DisplayName("Should update updatedAt timestamp")
        void updateMedication_ShouldUpdateTimestamp() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            Medication existing = aMedication().build();
            existing.setId(id);
            
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.of(existing));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(existing);

            // ACT
            medicationService.updateMedication(id, validRequest);

            // ASSERT
            verify(medicationRepository).save(captor.capture());
            Medication captured = captor.getValue();
            assertThat(captured.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent medication")
        void updateMedication_WhenNotFound_ThrowsException() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.updateMedication(id, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");

            verify(medicationRepository).findByIdAndIsActiveTrue(id);
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should validate expiration date on update")
        void updateMedication_WhenExpiredDate_ThrowsException() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            validRequest.setExpirationDate(LocalDate.now().minusDays(1));

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.updateMedication(id, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Expiration date must be in the future");

            // Validation happens before repository is accessed
            verify(medicationRepository, never()).findByIdAndIsActiveTrue(any());
            verify(medicationRepository, never()).save(any());
        }
    }

    // ========================================
    // DELETE TESTS
    // ========================================

    @Nested
    @DisplayName("Delete Medication Tests")
    class DeleteMedicationTests {

        @Test
        @DisplayName("Should soft delete medication successfully")
        void deleteMedication_Success() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            Medication existing = aMedication().build();
            existing.setId(id);
            
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.of(existing));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(existing);

            // ACT
            medicationService.deleteMedication(id);

            // ASSERT
            verify(medicationRepository).save(captor.capture());
            Medication captured = captor.getValue();
            assertThat(captured.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent medication")
        void deleteMedication_WhenNotFound_ThrowsException() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.deleteMedication(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");

            verify(medicationRepository).findByIdAndIsActiveTrue(id);
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not allow deleting medication that is currently prescribed")
        void deleteMedication_WhenPrescribed_ThrowsException() {
            // ARRANGE
            UUID id = UUID.randomUUID();
            Medication existing = aMedication().build();
            existing.setId(id);
            
            when(medicationRepository.findByIdAndIsActiveTrue(id))
                    .thenReturn(Optional.of(existing));
            // TODO: Add prescription check when prescription feature is implemented

            // ACT & ASSERT
            // This test will be implemented when we add prescription management
            // For now, soft delete should work
            medicationService.deleteMedication(id);
            
            verify(medicationRepository).save(any(Medication.class));
        }
    }

    // ========================================
    // INVENTORY QUERY TESTS
    // ========================================

    @Nested
    @DisplayName("Inventory Query Tests")
    class InventoryQueryTests {

        @Test
        @DisplayName("Should get low stock medications")
        void getLowStockMedications_Success() {
            // ARRANGE
            Medication lowStock1 = aMedication().lowStock().withName("Low 1").build();
            Medication lowStock2 = aMedication().lowStock().withName("Low 2").build();
            when(medicationRepository.findLowStockMedications())
                    .thenReturn(Arrays.asList(lowStock1, lowStock2));

            // ACT
            List<MedicationResponse> responses = medicationService.getLowStockMedications();

            // ASSERT
            assertThat(responses).hasSize(2);
            assertThat(responses).allMatch(MedicationResponse::getIsLowStock);
            verify(medicationRepository).findLowStockMedications();
        }

        @Test
        @DisplayName("Should get expired medications")
        void getExpiredMedications_Success() {
            // ARRANGE
            Medication expired1 = aMedication().expired().withName("Expired 1").build();
            Medication expired2 = aMedication().expired().withName("Expired 2").build();
            when(medicationRepository.findExpiredMedications())
                    .thenReturn(Arrays.asList(expired1, expired2));

            // ACT
            List<MedicationResponse> responses = medicationService.getExpiredMedications();

            // ASSERT
            assertThat(responses).hasSize(2);
            assertThat(responses).allMatch(MedicationResponse::getIsExpired);
            verify(medicationRepository).findExpiredMedications();
        }

        @Test
        @DisplayName("Should get expiring soon medications")
        void getExpiringSoonMedications_Success() {
            // ARRANGE
            LocalDate futureDate = LocalDate.now().plusDays(30);
            Medication expiring1 = aMedication().expiringSoon().withName("Expiring 1").build();
            Medication expiring2 = aMedication().expiringSoon().withName("Expiring 2").build();
            when(medicationRepository.findExpiringSoon(futureDate))
                    .thenReturn(Arrays.asList(expiring1, expiring2));

            // ACT
            List<MedicationResponse> responses = medicationService.getExpiringSoonMedications(30);

            // ASSERT
            assertThat(responses).hasSize(2);
            assertThat(responses).allMatch(MedicationResponse::getIsExpiringSoon);
            verify(medicationRepository).findExpiringSoon(futureDate);
        }

        @Test
        @DisplayName("Should default to 30 days for expiring soon")
        void getExpiringSoonMedications_DefaultDays() {
            // ARRANGE
            LocalDate futureDate = LocalDate.now().plusDays(30);
            when(medicationRepository.findExpiringSoon(futureDate))
                    .thenReturn(Arrays.asList());

            // ACT
            List<MedicationResponse> responses = medicationService.getExpiringSoonMedications(null);

            // ASSERT
            verify(medicationRepository).findExpiringSoon(futureDate);
        }
    }
}
