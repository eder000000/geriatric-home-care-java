package com.geriatriccare.unit.service;

import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
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
@DisplayName("Medication Inventory Service Tests")
class MedicationInventoryServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    private UUID medicationId;
    private Medication testMedication;

    @BeforeEach
    void setUp() {
        medicationId = UUID.randomUUID();
        testMedication = aMedication()
                .withQuantity(100)
                .withReorderLevel(20)
                .build();
        testMedication.setId(medicationId);
    }

    // ========================================
    // STOCK ADJUSTMENT TESTS
    // ========================================

    @Nested
    @DisplayName("Add Stock Tests")
    class AddStockTests {

        @Test
        @DisplayName("Should add stock successfully")
        void addStock_Success() {
            // ARRANGE
            int quantityToAdd = 50;
            int originalQuantity = testMedication.getQuantityInStock();
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(testMedication);

            // ACT
            MedicationResponse response = medicationService.addStock(medicationId, quantityToAdd);

            // ASSERT
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            verify(medicationRepository).save(captor.capture());
            
            Medication saved = captor.getValue();
            assertThat(saved.getQuantityInStock()).isEqualTo(originalQuantity + quantityToAdd);
        }

        @Test
        @DisplayName("Should throw exception when adding negative quantity")
        void addStock_WhenNegativeQuantity_ThrowsException() {
            // ARRANGE
            int negativeQuantity = -10;

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.addStock(medicationId, negativeQuantity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity to add must be positive");

            verify(medicationRepository, never()).findByIdAndIsActiveTrue(any());
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when adding zero quantity")
        void addStock_WhenZeroQuantity_ThrowsException() {
            // ARRANGE
            int zeroQuantity = 0;

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.addStock(medicationId, zeroQuantity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity to add must be positive");

            verify(medicationRepository, never()).findByIdAndIsActiveTrue(any());
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void addStock_WhenMedicationNotFound_ThrowsException() {
            // ARRANGE
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.addStock(medicationId, 50))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");

            verify(medicationRepository).findByIdAndIsActiveTrue(medicationId);
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update updatedAt timestamp when adding stock")
        void addStock_ShouldUpdateTimestamp() {
            // ARRANGE
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(testMedication);

            // ACT
            medicationService.addStock(medicationId, 50);

            // ASSERT
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            verify(medicationRepository).save(captor.capture());
            
            Medication saved = captor.getValue();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle large quantity additions")
        void addStock_WhenLargeQuantity_Success() {
            // ARRANGE
            int largeQuantity = 10000;
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(testMedication);

            // ACT
            medicationService.addStock(medicationId, largeQuantity);

            // ASSERT
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            verify(medicationRepository).save(captor.capture());
            
            Medication saved = captor.getValue();
            assertThat(saved.getQuantityInStock()).isEqualTo(100 + largeQuantity);
        }
    }

    @Nested
    @DisplayName("Remove Stock Tests")
    class RemoveStockTests {

        @Test
        @DisplayName("Should remove stock successfully")
        void removeStock_Success() {
            // ARRANGE
            int quantityToRemove = 30;
            int originalQuantity = testMedication.getQuantityInStock();
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(testMedication);

            // ACT
            MedicationResponse response = medicationService.removeStock(medicationId, quantityToRemove);

            // ASSERT
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            verify(medicationRepository).save(captor.capture());
            
            Medication saved = captor.getValue();
            assertThat(saved.getQuantityInStock()).isEqualTo(originalQuantity - quantityToRemove);
        }

        @Test
        @DisplayName("Should throw exception when removing more than available")
        void removeStock_WhenInsufficientStock_ThrowsException() {
            // ARRANGE
            testMedication.setQuantityInStock(50);
            int quantityToRemove = 60;
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.removeStock(medicationId, quantityToRemove))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(medicationRepository).findByIdAndIsActiveTrue(medicationId);
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow removing exact available quantity")
        void removeStock_WhenExactQuantity_Success() {
            // ARRANGE
            testMedication.setQuantityInStock(50);
            int quantityToRemove = 50;
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class)))
                    .thenReturn(testMedication);

            // ACT
            medicationService.removeStock(medicationId, quantityToRemove);

            // ASSERT
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            verify(medicationRepository).save(captor.capture());
            
            Medication saved = captor.getValue();
            assertThat(saved.getQuantityInStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw exception when removing negative quantity")
        void removeStock_WhenNegativeQuantity_ThrowsException() {
            // ARRANGE
            int negativeQuantity = -10;

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.removeStock(medicationId, negativeQuantity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity to remove must be positive");

            verify(medicationRepository, never()).findByIdAndIsActiveTrue(any());
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when removing zero quantity")
        void removeStock_WhenZeroQuantity_ThrowsException() {
            // ARRANGE
            int zeroQuantity = 0;

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.removeStock(medicationId, zeroQuantity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity to remove must be positive");

            verify(medicationRepository, never()).findByIdAndIsActiveTrue(any());
            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void removeStock_WhenMedicationNotFound_ThrowsException() {
            // ARRANGE
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.removeStock(medicationId, 10))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");

            verify(medicationRepository).findByIdAndIsActiveTrue(medicationId);
            verify(medicationRepository, never()).save(any());
        }
    }

    // ========================================
    // REORDER CALCULATION TESTS
    // ========================================

    @Nested
    @DisplayName("Reorder Calculation Tests")
    class ReorderCalculationTests {

        @Test
        @DisplayName("Should calculate reorder quantity for low stock medication")
        void calculateReorderQuantity_WhenLowStock_ReturnsQuantity() {
            // ARRANGE
            testMedication.setQuantityInStock(5);
            testMedication.setReorderLevel(20);
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            // ACT
            int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

            // ASSERT
            // Should reorder to reach 2x reorder level for safety stock
            assertThat(reorderQuantity).isEqualTo(35); // (20 * 2) - 5 = 35
        }

        @Test
        @DisplayName("Should return zero when stock is adequate")
        void calculateReorderQuantity_WhenAdequateStock_ReturnsZero() {
            // ARRANGE
            testMedication.setQuantityInStock(100);
            testMedication.setReorderLevel(20);
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            // ACT
            int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

            // ASSERT
            assertThat(reorderQuantity).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle medication at exact reorder level")
        void calculateReorderQuantity_AtReorderLevel_ReturnsQuantity() {
            // ARRANGE
            testMedication.setQuantityInStock(20);
            testMedication.setReorderLevel(20);
            
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            // ACT
            int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

            // ASSERT
            // At reorder level, reorder to 2x reorder level
            assertThat(reorderQuantity).isEqualTo(20); // (20 * 2) - 20 = 20
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void calculateReorderQuantity_WhenNotFound_ThrowsException() {
            // ARRANGE
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> medicationService.calculateReorderQuantity(medicationId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");
        }
    }

    // ========================================
    // BATCH OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Batch Operations Tests")
    class BatchOperationsTests {

        @Test
        @DisplayName("Should check multiple medications for low stock")
        void checkLowStockBatch_Success() {
        // ARRANGE
        Medication lowStock1 = aMedication().lowStock().withName("Low 1").build();
        Medication lowStock2 = aMedication().lowStock().withName("Low 2").build();
        
        // Mock the repository to return low stock medications directly
        when(medicationRepository.findLowStockMedications())
                .thenReturn(Arrays.asList(lowStock1, lowStock2));

        // ACT
        List<MedicationResponse> lowStockMeds = medicationService.getLowStockMedications();

        // ASSERT - should get both low stock medications
        assertThat(lowStockMeds)
                .hasSize(2)
                .extracting(MedicationResponse::getName)
                .containsExactlyInAnyOrder("Low 1", "Low 2");
        
        verify(medicationRepository).findLowStockMedications();
        }

        @Test
        @DisplayName("Should check expiration status for multiple medications")
        void checkExpirationBatch_Success() {
            // ARRANGE
            Medication expired1 = aMedication().expired().withName("Expired 1").build();
            Medication expired2 = aMedication().expired().withName("Expired 2").build();
            Medication valid = aMedication().notExpiring().withName("Valid").build();
            
            when(medicationRepository.findExpiredMedications())
                    .thenReturn(Arrays.asList(expired1, expired2));

            // ACT
            List<MedicationResponse> expiredMeds = medicationService.getExpiredMedications();

            // ASSERT
            assertThat(expiredMeds)
                    .hasSize(2)
                    .extracting(MedicationResponse::getName)
                    .containsExactlyInAnyOrder("Expired 1", "Expired 2");
        }

        @Test
        @DisplayName("Should get medications expiring within custom days")
        void getExpiringSoonWithCustomDays_Success() {
            // ARRANGE
            int customDays = 60;
            LocalDate futureDate = LocalDate.now().plusDays(customDays);
            
            Medication expiring1 = aMedication()
                    .withExpirationDate(LocalDate.now().plusDays(30))
                    .withName("Expiring 1")
                    .build();
            Medication expiring2 = aMedication()
                    .withExpirationDate(LocalDate.now().plusDays(45))
                    .withName("Expiring 2")
                    .build();
            
            when(medicationRepository.findExpiringSoon(futureDate))
                    .thenReturn(Arrays.asList(expiring1, expiring2));

            // ACT
            List<MedicationResponse> expiringMeds = medicationService.getExpiringSoonMedications(customDays);

            // ASSERT
            assertThat(expiringMeds).hasSize(2);
            verify(medicationRepository).findExpiringSoon(futureDate);
        }
    }

    // ========================================
    // INVENTORY SUMMARY TESTS
    // ========================================

    @Nested
    @DisplayName("Inventory Summary Tests")
    class InventorySummaryTests {

        @Test
        @DisplayName("Should get total medication count")
        void getTotalMedicationCount_Success() {
            // ARRANGE
            Medication med1 = aMedication().build();
            Medication med2 = aMedication().build();
            Medication med3 = aMedication().build();
            
            when(medicationRepository.findByIsActiveTrue())
                    .thenReturn(Arrays.asList(med1, med2, med3));

            // ACT
            int totalCount = medicationService.getTotalMedicationCount();

            // ASSERT
            assertThat(totalCount).isEqualTo(3);
        }

        @Test
        @DisplayName("Should get total stock quantity across all medications")
        void getTotalStockQuantity_Success() {
            // ARRANGE
            Medication med1 = aMedication().withQuantity(100).build();
            Medication med2 = aMedication().withQuantity(50).build();
            Medication med3 = aMedication().withQuantity(75).build();
            
            when(medicationRepository.findByIsActiveTrue())
                    .thenReturn(Arrays.asList(med1, med2, med3));

            // ACT
            int totalQuantity = medicationService.getTotalStockQuantity();

            // ASSERT
            assertThat(totalQuantity).isEqualTo(225);
        }

        @Test
        @DisplayName("Should count medications needing reorder")
        void countMedicationsNeedingReorder_Success() {
            // ARRANGE
            Medication lowStock1 = aMedication().lowStock().build();
            Medication lowStock2 = aMedication().lowStock().build();
            
            when(medicationRepository.findLowStockMedications())
                    .thenReturn(Arrays.asList(lowStock1, lowStock2));

            // ACT
            int reorderCount = medicationService.countMedicationsNeedingReorder();

            // ASSERT
            assertThat(reorderCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count expired medications")
        void countExpiredMedications_Success() {
            // ARRANGE
            Medication expired1 = aMedication().expired().build();
            Medication expired2 = aMedication().expired().build();
            Medication expired3 = aMedication().expired().build();
            
            when(medicationRepository.findExpiredMedications())
                    .thenReturn(Arrays.asList(expired1, expired2, expired3));

            // ACT
            int expiredCount = medicationService.countExpiredMedications();

            // ASSERT
            assertThat(expiredCount).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return zero when no medications exist")
        void getTotalMedicationCount_WhenEmpty_ReturnsZero() {
            // ARRANGE
            when(medicationRepository.findByIsActiveTrue())
                    .thenReturn(Arrays.asList());

            // ACT
            int totalCount = medicationService.getTotalMedicationCount();

            // ASSERT
            assertThat(totalCount).isEqualTo(0);
        }
    }
}