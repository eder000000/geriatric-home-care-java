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
@DisplayName("MedicationInventoryService Tests")
class MedicationInventoryServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    private Medication testMedication;
    private UUID medicationId;

    @BeforeEach
    void setUp() {
        medicationId = UUID.randomUUID();
        testMedication = aMedication()
                .withId(medicationId)
                .withName("Test Medication")
                .withQuantity(50)
                .withReorderLevel(10)
                .build();
    }

    @Nested
    @DisplayName("Add Stock Tests")
    class AddStockTests {

        @Test
        @DisplayName("Should add stock successfully")
        void addStock_Success() {
            int quantityToAdd = 20;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

            MedicationResponse response = medicationService.adjustStock(medicationId, quantityToAdd);

            assertThat(response).isNotNull();
            assertThat(testMedication.getQuantityInStock()).isEqualTo(70);
            verify(medicationRepository).findByIdAndIsActiveTrue(medicationId);
            verify(medicationRepository).save(testMedication);
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void addStock_WhenMedicationNotFound_ThrowsException() {
            int quantityToAdd = 20;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicationService.adjustStock(medicationId, quantityToAdd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");
        }

        @Test
        @DisplayName("Should throw exception when adding negative quantity")
        void addStock_WhenNegativeQuantity_ThrowsException() {
            int negativeQuantity = -10;
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> medicationService.adjustStock(randomId, negativeQuantity))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw exception when adding zero quantity")
        void addStock_WhenZeroQuantity_ThrowsException() {
            int zeroQuantity = 0;
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> medicationService.adjustStock(randomId, zeroQuantity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity change cannot be zero");
        }

        @Test
        @DisplayName("Should handle large quantity additions")
        void addStock_WhenLargeQuantity_Success() {
            int largeQuantity = 1000;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

            medicationService.adjustStock(medicationId, largeQuantity);

            assertThat(testMedication.getQuantityInStock()).isEqualTo(1050);
        }

        @Test
        @DisplayName("Should update medication timestamp")
        void addStock_ShouldUpdateTimestamp() {
            int quantityToAdd = 10;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

            medicationService.adjustStock(medicationId, quantityToAdd);

            verify(medicationRepository).save(testMedication);
        }
    }

    @Nested
    @DisplayName("Remove Stock Tests")
    class RemoveStockTests {

        @Test
        @DisplayName("Should remove stock successfully")
        void removeStock_Success() {
            int quantityToRemove = 20;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

            MedicationResponse response = medicationService.adjustStock(medicationId, -quantityToRemove);

            assertThat(response).isNotNull();
            assertThat(testMedication.getQuantityInStock()).isEqualTo(30);
            verify(medicationRepository).findByIdAndIsActiveTrue(medicationId);
            verify(medicationRepository).save(testMedication);
        }

        @Test
        @DisplayName("Should throw exception when removing more than available")
        void removeStock_WhenInsufficientStock_ThrowsException() {
            int quantityToRemove = 100;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            assertThatThrownBy(() -> medicationService.adjustStock(medicationId, -quantityToRemove))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should throw exception when medication not found")
        void removeStock_WhenMedicationNotFound_ThrowsException() {
            int quantityToRemove = 10;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicationService.adjustStock(medicationId, -quantityToRemove))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Medication not found");
        }

        @Test
        @DisplayName("Should throw exception when removing negative quantity")
        void removeStock_WhenNegativeQuantity_ThrowsException() {
            int negativeQuantity = -10;
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> medicationService.adjustStock(randomId, negativeQuantity))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw exception when removing zero quantity")
        void removeStock_WhenZeroQuantity_ThrowsException() {
            int zeroQuantity = 0;
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> medicationService.adjustStock(randomId, zeroQuantity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Quantity change cannot be zero");
        }

        @Test
        @DisplayName("Should allow removing exact stock quantity")
        void removeStock_WhenExactQuantity_Success() {
            int exactQuantity = 50;
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));
            when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

            medicationService.adjustStock(medicationId, -exactQuantity);

            assertThat(testMedication.getQuantityInStock()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Reorder Calculation Tests")
    class ReorderCalculationTests {

        @Test
        @DisplayName("Should calculate reorder quantity when low stock")
        void calculateReorderQuantity_WhenLowStock_ReturnsQuantity() {
            testMedication.setQuantityInStock(5);
            testMedication.setReorderLevel(10);
            when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                    .thenReturn(Optional.of(testMedication));

            int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

            assertThat(reorderQuantity).isEqualTo(15);
        }

        @Test
        @DisplayName("Should calculate reorder quantity when at reorder level")
    void calculateReorderQuantity_AtReorderLevel_ReturnsQuantity() {
        testMedication.setQuantityInStock(10);
        testMedication.setReorderLevel(10);
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

        assertThat(reorderQuantity).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return zero when stock is sufficient")
    void calculateReorderQuantity_WhenSufficientStock_ReturnsZero() {
        testMedication.setQuantityInStock(50);
        testMedication.setReorderLevel(10);
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

        assertThat(reorderQuantity).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle zero stock")
    void calculateReorderQuantity_WhenZeroStock_ReturnsQuantity() {
        testMedication.setQuantityInStock(0);
        testMedication.setReorderLevel(10);
        when(medicationRepository.findByIdAndIsActiveTrue(medicationId))
                .thenReturn(Optional.of(testMedication));

        int reorderQuantity = medicationService.calculateReorderQuantity(medicationId);

        assertThat(reorderQuantity).isEqualTo(20);
    }
}

@Nested
@DisplayName("Batch Operations Tests")
class BatchOperationsTests {

    @Test
    @DisplayName("Should get low stock medications")
    void getLowStockMedications_Success() {
        Medication lowStock1 = aMedication().lowStock().build();
        Medication lowStock2 = aMedication().lowStock().build();
        when(medicationRepository.findLowStockMedications())
                .thenReturn(Arrays.asList(lowStock1, lowStock2));

        List<MedicationResponse> responses = medicationService.findLowStock();

        assertThat(responses).hasSize(2);
        verify(medicationRepository).findLowStockMedications();
    }

    @Test
    @DisplayName("Should get expired medications")
    void getExpiredMedications_Success() {
        Medication expired1 = aMedication().expired().build();
        Medication expired2 = aMedication().expired().build();
        when(medicationRepository.findExpiredMedications())
                .thenReturn(Arrays.asList(expired1, expired2));

        List<MedicationResponse> responses = medicationService.findExpired();

        assertThat(responses).hasSize(2);
        verify(medicationRepository).findExpiredMedications();
    }

    @Test
    @DisplayName("Should get expiring soon medications with custom days")
    void getExpiringSoonWithCustomDays_Success() {
        int days = 60;
        LocalDate futureDate = LocalDate.now().plusDays(days);
        Medication expiring1 = aMedication().expiringSoon().build();
        Medication expiring2 = aMedication().expiringSoon().build();
        when(medicationRepository.findByExpirationDateBeforeAndIsActiveTrue(futureDate))
                .thenReturn(Arrays.asList(expiring1, expiring2));

        List<MedicationResponse> responses = medicationService.findExpiringSoon(days);

        assertThat(responses).hasSize(2);
        verify(medicationRepository).findByExpirationDateBeforeAndIsActiveTrue(futureDate);
    }
}

@Nested
@DisplayName("Inventory Summary Tests")
class InventorySummaryTests {

    @Test
    @DisplayName("Should get total medication count")
    void getTotalMedicationCount_Success() {
        when(medicationRepository.findByIsActiveTrue())
                .thenReturn(Arrays.asList(
                        aMedication().build(),
                        aMedication().build(),
                        aMedication().build()
                ));

        int count = medicationService.countAllMedications();

        assertThat(count).isEqualTo(3);
        verify(medicationRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should get total stock quantity")
    void getTotalStockQuantity_Success() {
        when(medicationRepository.findByIsActiveTrue())
                .thenReturn(Arrays.asList(
                        aMedication().withQuantity(50).build(),
                        aMedication().withQuantity(30).build(),
                        aMedication().withQuantity(20).build()
                ));

        int totalStock = medicationService.calculateTotalStock();

        assertThat(totalStock).isEqualTo(100);
        verify(medicationRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should count medications needing reorder")
    void countMedicationsNeedingReorder_Success() {
        when(medicationRepository.findLowStockMedications())
                .thenReturn(Arrays.asList(
                        aMedication().lowStock().build(),
                        aMedication().lowStock().build()
                ));

        int count = medicationService.countNeedingReorder();

        assertThat(count).isEqualTo(2);
        verify(medicationRepository).findLowStockMedications();
    }

    @Test
    @DisplayName("Should count expired medications")
    void countExpiredMedications_Success() {
        when(medicationRepository.findExpiredMedications())
                .thenReturn(Arrays.asList(
                        aMedication().expired().build(),
                        aMedication().expired().build(),
                        aMedication().expired().build()
                ));

        int count = medicationService.countExpired();

        assertThat(count).isEqualTo(3);
        verify(medicationRepository).findExpiredMedications();
    }

    @Test
    @DisplayName("Should handle empty inventory")
    void getTotalMedicationCount_WhenEmpty_ReturnsZero() {
        when(medicationRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        int count = medicationService.countAllMedications();

        assertThat(count).isEqualTo(0);
    }
}
}
