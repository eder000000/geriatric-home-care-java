package com.geriatriccare.unit.entity;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Medication Entity Tests")
class MedicationTest {

    private Medication medication;

    @BeforeEach
    void setUp() {
        medication = new Medication();
        medication.setName("Aspirin");
        medication.setGenericName("Acetylsalicylic Acid");
        medication.setDosage("500mg");
        medication.setForm(MedicationForm.TABLET);
        medication.setManufacturer("Bayer");
        medication.setExpirationDate(LocalDate.now().plusYears(1));
        medication.setQuantityInStock(100);
        medication.setReorderLevel(20);
    }

    @Test
    @DisplayName("Should create medication with all fields")
    void createMedication_WithAllFields_Success() {
        assertThat(medication.getName()).isEqualTo("Aspirin");
        assertThat(medication.getGenericName()).isEqualTo("Acetylsalicylic Acid");
        assertThat(medication.getDosage()).isEqualTo("500mg");
        assertThat(medication.getForm()).isEqualTo(MedicationForm.TABLET);
        assertThat(medication.getManufacturer()).isEqualTo("Bayer");
        assertThat(medication.getQuantityInStock()).isEqualTo(100);
        assertThat(medication.getReorderLevel()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should have isActive as true by default")
    void createMedication_DefaultIsActive_IsTrue() {
        Medication newMed = new Medication();
        assertThat(newMed.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should allow setting isActive to false")
    void setIsActive_ToFalse_Success() {
        medication.setIsActive(false);
        assertThat(medication.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should update quantity in stock")
    void updateQuantity_Success() {
        medication.setQuantityInStock(150);
        assertThat(medication.getQuantityInStock()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should update reorder level")
    void updateReorderLevel_Success() {
        medication.setReorderLevel(30);
        assertThat(medication.getReorderLevel()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should update expiration date")
    void updateExpirationDate_Success() {
        LocalDate newDate = LocalDate.now().plusYears(2);
        medication.setExpirationDate(newDate);
        assertThat(medication.getExpirationDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("Should support all medication forms")
    void setForm_AllTypes_Success() {
        medication.setForm(MedicationForm.CAPSULE);
        assertThat(medication.getForm()).isEqualTo(MedicationForm.CAPSULE);
        
        medication.setForm(MedicationForm.LIQUID);
        assertThat(medication.getForm()).isEqualTo(MedicationForm.LIQUID);
        
        medication.setForm(MedicationForm.INJECTION);
        assertThat(medication.getForm()).isEqualTo(MedicationForm.INJECTION);
    }

    @Test
    @DisplayName("Should allow null generic name")
    void setGenericName_Null_Success() {
        medication.setGenericName(null);
        assertThat(medication.getGenericName()).isNull();
    }

    @Test
    @DisplayName("Should allow null manufacturer")
    void setManufacturer_Null_Success() {
        medication.setManufacturer(null);
        assertThat(medication.getManufacturer()).isNull();
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void setQuantity_Zero_Success() {
        medication.setQuantityInStock(0);
        assertThat(medication.getQuantityInStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle zero reorder level")
    void setReorderLevel_Zero_Success() {
        medication.setReorderLevel(0);
        assertThat(medication.getReorderLevel()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should set and get ID")
    void setId_Success() {
        medication.setId(java.util.UUID.randomUUID());
        assertThat(medication.getId()).isNotNull();
    }
}