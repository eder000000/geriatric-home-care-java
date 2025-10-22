package com.geriatriccare.builders;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.geriatriccare.builders.MedicationTestBuilder.aMedication;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MedicationTestBuilder Tests")
class MedicationTestBuilderTest {

    @Test
    @DisplayName("Should create medication with sensible defaults")
    void shouldCreateWithDefaults() {
        Medication medication = aMedication().build();

        assertThat(medication.getId()).isNotNull();
        assertThat(medication.getName()).isEqualTo("Test Medication");
        assertThat(medication.getDosage()).isEqualTo("100mg");
        assertThat(medication.getForm()).isEqualTo(MedicationForm.TABLET);
        assertThat(medication.getExpirationDate()).isAfter(LocalDate.now());
        assertThat(medication.getQuantityInStock()).isEqualTo(100);
        assertThat(medication.getReorderLevel()).isEqualTo(20);
        assertThat(medication.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should override name")
    void shouldOverrideName() {
        Medication medication = aMedication()
                .withName("Aspirin")
                .build();

        assertThat(medication.getName()).isEqualTo("Aspirin");
    }

    @Test
    @DisplayName("Should set form using shortcuts")
    void shouldSetFormUsingShortcuts() {
        Medication tablet = aMedication().tablet().build();
        Medication capsule = aMedication().capsule().build();
        Medication liquid = aMedication().liquid().build();
        Medication injection = aMedication().injection().build();
        Medication topical = aMedication().topical().build();

        assertThat(tablet.getForm()).isEqualTo(MedicationForm.TABLET);
        assertThat(capsule.getForm()).isEqualTo(MedicationForm.CAPSULE);
        assertThat(liquid.getForm()).isEqualTo(MedicationForm.LIQUID);
        assertThat(injection.getForm()).isEqualTo(MedicationForm.INJECTION);
        assertThat(topical.getForm()).isEqualTo(MedicationForm.TOPICAL);
    }

    @Test
    @DisplayName("Should create low stock medication")
    void shouldCreateLowStock() {
        Medication medication = aMedication()
                .lowStock()
                .build();

        assertThat(medication.getQuantityInStock()).isEqualTo(5);
        assertThat(medication.getReorderLevel()).isEqualTo(10);
        assertThat(medication.getQuantityInStock())
                .isLessThanOrEqualTo(medication.getReorderLevel());
    }

    @Test
    @DisplayName("Should create expired medication")
    void shouldCreateExpired() {
        Medication medication = aMedication()
                .expired()
                .build();

        assertThat(medication.getExpirationDate()).isBefore(LocalDate.now());
    }

    @Test
    @DisplayName("Should create expiring soon medication")
    void shouldCreateExpiringSoon() {
        Medication medication = aMedication()
                .expiringSoon()
                .build();

        LocalDate expiration = medication.getExpirationDate();
        assertThat(expiration).isAfter(LocalDate.now());
        assertThat(expiration).isBefore(LocalDate.now().plusDays(31));
    }

    @Test
    @DisplayName("Should create aspirin with all fields")
    void shouldCreateAspirin() {
        Medication aspirin = aMedication()
                .aspirin()
                .build();

        assertThat(aspirin.getName()).isEqualTo("Aspirin");
        assertThat(aspirin.getGenericName()).isEqualTo("Acetylsalicylic Acid");
        assertThat(aspirin.getDosage()).isEqualTo("500mg");
        assertThat(aspirin.getForm()).isEqualTo(MedicationForm.TABLET);
        assertThat(aspirin.getManufacturer()).isEqualTo("Bayer");
    }

    @Test
    @DisplayName("Should chain multiple methods")
    void shouldChainMethods() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Medication medication = aMedication()
                .withName("Complex Med")
                .injection()
                .lowStock()
                .expiringSoon()
                .withCreatedBy(user)
                .inactive()
                .build();

        assertThat(medication.getName()).isEqualTo("Complex Med");
        assertThat(medication.getForm()).isEqualTo(MedicationForm.INJECTION);
        assertThat(medication.getQuantityInStock()).isEqualTo(5);
        assertThat(medication.getExpirationDate()).isBefore(LocalDate.now().plusDays(31));
        assertThat(medication.getCreatedBy()).isEqualTo(user);
        assertThat(medication.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should create multiple different medications")
    void shouldCreateMultipleMedications() {
        Medication aspirin = aMedication().aspirin().build();
        Medication insulin = aMedication().insulin().build();
        Medication metformin = aMedication().metformin().build();

        assertThat(aspirin.getName()).isEqualTo("Aspirin");
        assertThat(insulin.getName()).isEqualTo("Insulin");
        assertThat(metformin.getName()).isEqualTo("Metformin");

        // Each should be independent
        assertThat(aspirin).isNotSameAs(insulin);
        assertThat(insulin).isNotSameAs(metformin);
    }
}