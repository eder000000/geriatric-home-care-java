package com.geriatriccare.unit.repository;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;
import com.geriatriccare.entity.UserRole;
import com.geriatriccare.repository.MedicationRepository;
import com.geriatriccare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Medication Repository Tests")
class MedicationRepositoryTest {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Medication aspirin;
    private Medication insulin;
    private Medication expiredMed;
    private Medication lowStockMed;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.OWNER);
        testUser.setCreatedAt(LocalDateTime.now());  
        testUser.setUpdatedAt(LocalDateTime.now()); 
        testUser = userRepository.save(testUser);

        // Create test medications
        aspirin = createMedication("Aspirin", "Acetylsalicylic Acid", "500mg", 
            MedicationForm.TABLET, LocalDate.now().plusYears(1), 100, 20);
        aspirin = medicationRepository.save(aspirin);

        insulin = createMedication("Insulin", "Human Insulin", "100 units/mL",
            MedicationForm.INJECTION, LocalDate.now().plusMonths(6), 50, 10);
        insulin = medicationRepository.save(insulin);

        expiredMed = createMedication("Expired Med", null, "10mg",
            MedicationForm.TABLET, LocalDate.now().minusDays(1), 30, 5);
        expiredMed = medicationRepository.save(expiredMed);

        lowStockMed = createMedication("Low Stock Med", null, "25mg",
            MedicationForm.CAPSULE, LocalDate.now().plusMonths(3), 5, 10);
        lowStockMed = medicationRepository.save(lowStockMed);
    }

    // ========== BASIC CRUD TESTS ==========

    @Test
    @DisplayName("Should save medication successfully")
    void shouldSaveMedicationSuccessfully() {
        Medication newMed = createMedication("New Med", null, "50mg",
            MedicationForm.LIQUID, LocalDate.now().plusYears(1), 75, 15);

        Medication saved = medicationRepository.save(newMed);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Med");
    }

    @Test
    @DisplayName("Should find medication by ID")
    void shouldFindMedicationById() {
        Optional<Medication> found = medicationRepository.findById(aspirin.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Aspirin");
    }

    @Test
    @DisplayName("Should return empty when medication not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Medication> found = medicationRepository.findById(java.util.UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all medications")
    void shouldFindAllMedications() {
        List<Medication> all = medicationRepository.findAll();

        assertThat(all).hasSize(4);
    }

    // ========== SOFT DELETE TESTS ==========

    @Test
    @DisplayName("Should find only active medications")
    void shouldFindOnlyActiveMedications() {
        // Soft delete one medication
        aspirin.setIsActive(false);
        medicationRepository.save(aspirin);

        List<Medication> active = medicationRepository.findByIsActiveTrue();

        assertThat(active).hasSize(3);
        assertThat(active).noneMatch(m -> m.getId().equals(aspirin.getId()));
    }

    @Test
    @DisplayName("Should find active medication by ID")
    void shouldFindActiveMedicationById() {
        Optional<Medication> found = medicationRepository.findByIdAndIsActiveTrue(aspirin.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Aspirin");
    }

    @Test
    @DisplayName("Should not find inactive medication by ID")
    void shouldNotFindInactiveMedicationById() {
        aspirin.setIsActive(false);
        medicationRepository.save(aspirin);

        Optional<Medication> found = medicationRepository.findByIdAndIsActiveTrue(aspirin.getId());

        assertThat(found).isEmpty();
    }

    // ========== SEARCH QUERY TESTS ==========

    @Test
    @DisplayName("Should find medications by name containing search term")
    void shouldFindMedicationsByNameContaining() {
        List<Medication> results = medicationRepository
            .findByNameContainingIgnoreCaseAndIsActiveTrue("asp");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Aspirin");
    }

    @Test
    @DisplayName("Should find medications ignoring case")
    void shouldFindMedicationsIgnoringCase() {
        List<Medication> results = medicationRepository
            .findByNameContainingIgnoreCaseAndIsActiveTrue("INSULIN");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Insulin");
    }

    @Test
    @DisplayName("Should return empty list when search term not found")
    void shouldReturnEmptyWhenSearchTermNotFound() {
        List<Medication> results = medicationRepository
            .findByNameContainingIgnoreCaseAndIsActiveTrue("NonExistent");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should find medications by form")
    void shouldFindMedicationsByForm() {
        List<Medication> tablets = medicationRepository
            .findByFormAndIsActiveTrue(MedicationForm.TABLET);

        assertThat(tablets).hasSize(2); // Aspirin and Expired Med
        assertThat(tablets).allMatch(m -> m.getForm() == MedicationForm.TABLET);
    }

    @Test
    @DisplayName("Should find medications by injection form")
    void shouldFindMedicationsByInjectionForm() {
        List<Medication> injections = medicationRepository
            .findByFormAndIsActiveTrue(MedicationForm.INJECTION);

        assertThat(injections).hasSize(1);
        assertThat(injections.get(0).getName()).isEqualTo("Insulin");
    }

    @Test
    @DisplayName("Should return empty list when no medications match form")
    void shouldReturnEmptyWhenNoMedicationsMatchForm() {
        List<Medication> topical = medicationRepository
            .findByFormAndIsActiveTrue(MedicationForm.TOPICAL);

        assertThat(topical).isEmpty();
    }

    // ========== INVENTORY QUERY TESTS ==========

    @Test
    @DisplayName("Should find low stock medications")
    void shouldFindLowStockMedications() {
        List<Medication> lowStock = medicationRepository.findLowStockMedications();

        assertThat(lowStock).hasSize(1);
        assertThat(lowStock.get(0).getName()).isEqualTo("Low Stock Med");
    }

    @Test
    @DisplayName("Should find medications at reorder level")
    void shouldFindMedicationsAtReorderLevel() {
        // Create medication exactly at reorder level
        Medication atReorder = createMedication("At Reorder", null, "10mg",
            MedicationForm.TABLET, LocalDate.now().plusMonths(6), 10, 10);
        medicationRepository.save(atReorder);

        List<Medication> lowStock = medicationRepository.findLowStockMedications();

        assertThat(lowStock).hasSizeGreaterThanOrEqualTo(2);
        assertThat(lowStock).anyMatch(m -> m.getName().equals("At Reorder"));
    }

    @Test
    @DisplayName("Should not include high stock medications in low stock results")
    void shouldNotIncludeHighStockMedications() {
        List<Medication> lowStock = medicationRepository.findLowStockMedications();

        assertThat(lowStock).noneMatch(m -> m.getName().equals("Aspirin"));
        assertThat(lowStock).noneMatch(m -> m.getName().equals("Insulin"));
    }

    @Test
    @DisplayName("Should find expired medications")
    void shouldFindExpiredMedications() {
        List<Medication> expired = medicationRepository.findExpiredMedications();

        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getName()).isEqualTo("Expired Med");
    }

    @Test
    @DisplayName("Should not include future medications in expired results")
    void shouldNotIncludeFutureMedicationsInExpiredResults() {
        List<Medication> expired = medicationRepository.findExpiredMedications();

        assertThat(expired).noneMatch(m -> m.getName().equals("Aspirin"));
        assertThat(expired).noneMatch(m -> m.getName().equals("Insulin"));
    }

    @Test
    @DisplayName("Should find medications expiring soon")
    void shouldFindMedicationsExpiringSoon() {
        // Create medication expiring in 15 days
        Medication expiringSoon = createMedication("Expiring Soon", null, "10mg",
            MedicationForm.TABLET, LocalDate.now().plusDays(15), 30, 5);
        medicationRepository.save(expiringSoon);

        LocalDate futureDate = LocalDate.now().plusDays(30);
        List<Medication> expiring = medicationRepository.findExpiringSoon(futureDate);

        assertThat(expiring).hasSizeGreaterThanOrEqualTo(1);
        assertThat(expiring).anyMatch(m -> m.getName().equals("Expiring Soon"));
    }

    @Test
    @DisplayName("Should not include expired medications in expiring soon results")
    void shouldNotIncludeExpiredInExpiringSoonResults() {
        LocalDate futureDate = LocalDate.now().plusDays(30);
        List<Medication> expiring = medicationRepository.findExpiringSoon(futureDate);

        assertThat(expiring).noneMatch(m -> m.getName().equals("Expired Med"));
    }

    @Test
    @DisplayName("Should not include far future medications in expiring soon results")
    void shouldNotIncludeFarFutureMedicationsInExpiringSoon() {
        LocalDate futureDate = LocalDate.now().plusDays(30);
        List<Medication> expiring = medicationRepository.findExpiringSoon(futureDate);

        assertThat(expiring).noneMatch(m -> m.getName().equals("Aspirin")); // Expires in 1 year
    }

    // ========== MANUFACTURER QUERY TESTS ==========

    @Test
    @DisplayName("Should find medications by manufacturer")
    void shouldFindMedicationsByManufacturer() {
        aspirin.setManufacturer("Bayer");
        medicationRepository.save(aspirin);

        List<Medication> byManufacturer = medicationRepository
            .findByManufacturerIgnoreCaseAndIsActiveTrue("Bayer");

        assertThat(byManufacturer).hasSize(1);
        assertThat(byManufacturer.get(0).getName()).isEqualTo("Aspirin");
    }

    @Test
    @DisplayName("Should find medications by manufacturer ignoring case")
    void shouldFindMedicationsByManufacturerIgnoringCase() {
        aspirin.setManufacturer("Bayer");
        medicationRepository.save(aspirin);

        List<Medication> byManufacturer = medicationRepository
            .findByManufacturerIgnoreCaseAndIsActiveTrue("BAYER");

        assertThat(byManufacturer).hasSize(1);
        assertThat(byManufacturer.get(0).getName()).isEqualTo("Aspirin");
    }

    // ========== HELPER METHOD ==========

    private Medication createMedication(String name, String genericName, String dosage,
                                       MedicationForm form, LocalDate expirationDate,
                                       Integer quantity, Integer reorderLevel) {
        Medication med = new Medication();
        med.setName(name);
        med.setGenericName(genericName);
        med.setDosage(dosage);
        med.setForm(form);
        med.setExpirationDate(expirationDate);
        med.setQuantityInStock(quantity);
        med.setReorderLevel(reorderLevel);
        med.setCreatedAt(LocalDateTime.now());
        med.setUpdatedAt(LocalDateTime.now());
        med.setCreatedBy(testUser);
        med.setUpdatedBy(testUser);
        return med;
    }
}