package com.geriatriccare.unit.dto;

import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.geriatriccare.builders.MedicationTestBuilder.aMedication;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MedicationResponse Parameterized Tests")
class MedicationResponseParameterizedTest {

    @ParameterizedTest(name = "quantity={0}, reorder={1} → isLowStock={2}")
    @CsvSource({
        "0, 10, true",   // Zero quantity
        "5, 10, true",   // Below reorder
        "10, 10, true",  // At reorder level
        "11, 10, false", // Just above reorder
        "50, 10, false", // Well above reorder
        "100, 0, false", // Edge case: zero reorder level
        "0, 0, true"     // Both zero
    })
    @DisplayName("Should calculate isLowStock correctly")
    void isLowStock_VariousQuantities(int quantity, int reorder, boolean expectedLowStock) {
        // ARRANGE - Using builder!
        Medication medication = aMedication()
                .withQuantity(quantity)
                .withReorderLevel(reorder)
                .build();

        // ACT
        MedicationResponse response = MedicationResponse.fromEntity(medication);

        // ASSERT
        assertThat(response.getIsLowStock()).isEqualTo(expectedLowStock);
    }

    @ParameterizedTest(name = "expires in {0} days → isExpired={1}, isExpiringSoon={2}")
    @MethodSource("expirationScenarios")
    @DisplayName("Should calculate expiration status correctly")
    void expirationStatus_VariousScenarios(int daysFromNow, boolean expectedExpired, boolean expectedExpiringSoon) {
        // ARRANGE
        Medication medication = aMedication()
                .withExpirationDate(LocalDate.now().plusDays(daysFromNow))
                .build();

        // ACT
        MedicationResponse response = MedicationResponse.fromEntity(medication);

        // ASSERT
        assertThat(response.getIsExpired()).isEqualTo(expectedExpired);
        assertThat(response.getIsExpiringSoon()).isEqualTo(expectedExpiringSoon);
    }

    static Stream<Arguments> expirationScenarios() {
        return Stream.of(
            //             days, expired, expiringSoon
            Arguments.of(-30,   true,    false),  // Expired 30 days ago
            Arguments.of(-1,    true,    false),  // Expired yesterday
            Arguments.of(0,     false,   true),   // Expires today
            Arguments.of(1,     false,   true),   // Expires tomorrow
            Arguments.of(15,    false,   true),   // Expires in 15 days
            Arguments.of(29,    false,   true),   // Expires in 29 days
            Arguments.of(30,    false,   true),   // Expires in exactly 30 days
            Arguments.of(31,    false,   false),  // Expires in 31 days
            Arguments.of(60,    false,   false),  // Expires in 60 days
            Arguments.of(365,   false,   false)   // Expires in 1 year
        );
    }
}