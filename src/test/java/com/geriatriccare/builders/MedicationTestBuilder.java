package com.geriatriccare.builders;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Builder for Medication entity.
 * Provides fluent API for creating test medications with sensible defaults.
 * 
 * Usage:
 * <pre>
 * Medication aspirin = aMedication()
 *     .withName("Aspirin")
 *     .tablet()
 *     .lowStock()
 *     .build();
 * </pre>
 */
public class MedicationTestBuilder {
    
    private Medication medication;
    
    private MedicationTestBuilder() {
        medication = new Medication();
        // Sensible defaults for all required fields
        medication.setId(UUID.randomUUID());
        medication.setName("Test Medication");
        medication.setDosage("100mg");
        medication.setForm(MedicationForm.TABLET);
        medication.setExpirationDate(LocalDate.now().plusYears(1));
        medication.setQuantityInStock(100);
        medication.setReorderLevel(20);
        medication.setIsActive(true);
        medication.setCreatedAt(LocalDateTime.now());
        medication.setUpdatedAt(LocalDateTime.now());
    }
    
    public static MedicationTestBuilder aMedication() {
        return new MedicationTestBuilder();
    }
    
    // ========== BASIC SETTERS ==========
    
    public MedicationTestBuilder withId(UUID id) {
        medication.setId(id);
        return this;
    }
    
    public MedicationTestBuilder withName(String name) {
        medication.setName(name);
        return this;
    }
    
    public MedicationTestBuilder withGenericName(String genericName) {
        medication.setGenericName(genericName);
        return this;
    }
    
    public MedicationTestBuilder withDosage(String dosage) {
        medication.setDosage(dosage);
        return this;
    }
    
    public MedicationTestBuilder withManufacturer(String manufacturer) {
        medication.setManufacturer(manufacturer);
        return this;
    }
    
    // ========== FORM SHORTCUTS ==========
    
    public MedicationTestBuilder tablet() {
        medication.setForm(MedicationForm.TABLET);
        return this;
    }
    
    public MedicationTestBuilder capsule() {
        medication.setForm(MedicationForm.CAPSULE);
        return this;
    }
    
    public MedicationTestBuilder liquid() {
        medication.setForm(MedicationForm.LIQUID);
        return this;
    }
    
    public MedicationTestBuilder injection() {
        medication.setForm(MedicationForm.INJECTION);
        return this;
    }
    
    public MedicationTestBuilder topical() {
        medication.setForm(MedicationForm.TOPICAL);
        return this;
    }
    
    public MedicationTestBuilder withForm(MedicationForm form) {
        medication.setForm(form);
        return this;
    }
    
    // ========== INVENTORY METHODS ==========
    
    public MedicationTestBuilder withQuantity(int quantity) {
        medication.setQuantityInStock(quantity);
        return this;
    }
    
    public MedicationTestBuilder withReorderLevel(int reorderLevel) {
        medication.setReorderLevel(reorderLevel);
        return this;
    }
    
    public MedicationTestBuilder lowStock() {
        medication.setQuantityInStock(5);
        medication.setReorderLevel(10);
        return this;
    }
    
    public MedicationTestBuilder atReorderLevel() {
        medication.setQuantityInStock(10);
        medication.setReorderLevel(10);
        return this;
    }
    
    public MedicationTestBuilder highStock() {
        medication.setQuantityInStock(200);
        medication.setReorderLevel(20);
        return this;
    }
    
    // ========== EXPIRATION METHODS ==========
    
    public MedicationTestBuilder withExpirationDate(LocalDate date) {
        medication.setExpirationDate(date);
        return this;
    }
    
    public MedicationTestBuilder expired() {
        medication.setExpirationDate(LocalDate.now().minusDays(1));
        return this;
    }
    
    public MedicationTestBuilder expiringSoon() {
        medication.setExpirationDate(LocalDate.now().plusDays(15));
        return this;
    }
    
    public MedicationTestBuilder notExpiring() {
        medication.setExpirationDate(LocalDate.now().plusYears(2));
        return this;
    }
    
    // ========== AUDIT FIELDS ==========
    
    public MedicationTestBuilder withCreatedBy(User user) {
        medication.setCreatedBy(user);
        return this;
    }
    
    public MedicationTestBuilder withUpdatedBy(User user) {
        medication.setUpdatedBy(user);
        return this;
    }
    
    public MedicationTestBuilder withCreatedAt(LocalDateTime createdAt) {
        medication.setCreatedAt(createdAt);
        return this;
    }
    
    public MedicationTestBuilder withUpdatedAt(LocalDateTime updatedAt) {
        medication.setUpdatedAt(updatedAt);
        return this;
    }
    
    // ========== LIFECYCLE METHODS ==========
    
    public MedicationTestBuilder inactive() {
        medication.setIsActive(false);
        return this;
    }
    
    public MedicationTestBuilder active() {
        medication.setIsActive(true);
        return this;
    }
    
    // ========== COMMON SCENARIOS ==========
    
    public MedicationTestBuilder aspirin() {
        medication.setName("Aspirin");
        medication.setGenericName("Acetylsalicylic Acid");
        medication.setDosage("500mg");
        medication.setForm(MedicationForm.TABLET);
        medication.setManufacturer("Bayer");
        return this;
    }
    
    public MedicationTestBuilder insulin() {
        medication.setName("Insulin");
        medication.setGenericName("Human Insulin");
        medication.setDosage("100 units/mL");
        medication.setForm(MedicationForm.INJECTION);
        return this;
    }
    
    public MedicationTestBuilder lisinopril() {
        medication.setName("Lisinopril");
        medication.setGenericName("Lisinopril");
        medication.setDosage("10mg");
        medication.setForm(MedicationForm.TABLET);
        return this;
    }
    
    public MedicationTestBuilder metformin() {
        medication.setName("Metformin");
        medication.setGenericName("Metformin HCl");
        medication.setDosage("500mg");
        medication.setForm(MedicationForm.TABLET);
        return this;
    }
    
    // ========== BUILD METHOD ==========
    
    public Medication build() {
        return medication;
    }
}
