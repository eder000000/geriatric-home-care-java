package com.geriatriccare.service;

import com.geriatriccare.dto.MedicationRequest;
import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.repository.MedicationRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    // ========================================
    // CREATE
    // ========================================

    public MedicationResponse createMedication(MedicationRequest request) {
        // Validate business rules
        validateMedicationRequest(request);

        // Create entity from request
        Medication medication = new Medication();
        mapRequestToEntity(request, medication);

        // Set audit fields
        medication.setCreatedAt(LocalDateTime.now());
        medication.setUpdatedAt(LocalDateTime.now());
        medication.setIsActive(true);

        // Save and return response
        Medication saved = medicationRepository.save(medication);
        return MedicationResponse.fromEntity(saved);
    }

    // ========================================
    // READ
    // ========================================

    public MedicationResponse getMedicationById(UUID id) {
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        return MedicationResponse.fromEntity(medication);
    }

    public List<MedicationResponse> getAllMedications() {
        return medicationRepository.findByIsActiveTrue()
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========================================
    // SEARCH
    // ========================================

    public List<MedicationResponse> searchByName(String name) {
        return medicationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name)
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> searchByForm(MedicationForm form) {
        return medicationRepository.findByFormAndIsActiveTrue(form)
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> searchByManufacturer(String manufacturer) {
        return medicationRepository.findByManufacturerIgnoreCaseAndIsActiveTrue(manufacturer)
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========================================
    // UPDATE
    // ========================================

    public MedicationResponse updateMedication(UUID id, MedicationRequest request) {
        // Validate business rules
        validateMedicationRequest(request);

        // Find existing medication
        Medication existing = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));

        // Update fields
        mapRequestToEntity(request, existing);

        // Update audit fields
        existing.setUpdatedAt(LocalDateTime.now());

        // Save and return response
        Medication updated = medicationRepository.save(existing);
        return MedicationResponse.fromEntity(updated);
    }

    // ========================================
    // DELETE
    // ========================================

    public void deleteMedication(UUID id) {
        // Find existing medication
        Medication existing = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));

        // TODO: Check if medication is currently prescribed
        // This will be implemented when prescription management is added

        // Soft delete
        existing.setIsActive(false);
        existing.setUpdatedAt(LocalDateTime.now());
        medicationRepository.save(existing);
    }

    // ========================================
    // INVENTORY QUERIES
    // ========================================

    public List<MedicationResponse> getLowStockMedications() {
        return medicationRepository.findLowStockMedications()
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> getExpiredMedications() {
        return medicationRepository.findExpiredMedications()
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> getExpiringSoonMedications(Integer days) {
        // Default to 30 days if not specified
        int daysToCheck = (days != null && days > 0) ? days : 30;
        LocalDate futureDate = LocalDate.now().plusDays(daysToCheck);

        return medicationRepository.findExpiringSoon(futureDate)
                .stream()
                .map(MedicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========================================
    // INVENTORY MANAGEMENT - NEW METHODS
    // ========================================

    /**
     * Add stock to a medication (e.g., receiving a shipment)
     */
    public MedicationResponse addStock(UUID medicationId, int quantity) {
        // Validate quantity
        if (quantity <= 0) {
            throw new RuntimeException("Quantity to add must be positive");
        }

        // Find medication
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + medicationId));

        // Add stock
        int newQuantity = medication.getQuantityInStock() + quantity;
        medication.setQuantityInStock(newQuantity);
        medication.setUpdatedAt(LocalDateTime.now());

        // Save and return
        Medication updated = medicationRepository.save(medication);
        return MedicationResponse.fromEntity(updated);
    }

    /**
     * Remove stock from a medication (e.g., dispensing)
     */
    public MedicationResponse removeStock(UUID medicationId, int quantity) {
        // Validate quantity
        if (quantity <= 0) {
            throw new RuntimeException("Quantity to remove must be positive");
        }

        // Find medication
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + medicationId));

        // Check sufficient stock
        if (medication.getQuantityInStock() < quantity) {
            throw new RuntimeException(
                String.format("Insufficient stock. Available: %d, Requested: %d",
                    medication.getQuantityInStock(), quantity)
            );
        }

        // Remove stock
        int newQuantity = medication.getQuantityInStock() - quantity;
        medication.setQuantityInStock(newQuantity);
        medication.setUpdatedAt(LocalDateTime.now());

        // Save and return
        Medication updated = medicationRepository.save(medication);
        return MedicationResponse.fromEntity(updated);
    }

    /**
     * Calculate how much stock needs to be reordered
     * Strategy: Reorder to reach 2x the reorder level (safety stock)
     */
    public int calculateReorderQuantity(UUID medicationId) {
        // Find medication
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + medicationId));

        int currentStock = medication.getQuantityInStock();
        int reorderLevel = medication.getReorderLevel();

        // If stock is adequate, no need to reorder
        if (currentStock > reorderLevel) {
            return 0;
        }

        // Calculate quantity to reach 2x reorder level (safety stock)
        int targetStock = reorderLevel * 2;
        return targetStock - currentStock;
    }

    // ========================================
    // INVENTORY SUMMARY METHODS
    // ========================================

    /**
     * Get total count of active medications
     */
    public int getTotalMedicationCount() {
        return medicationRepository.findByIsActiveTrue().size();
    }

    /**
     * Get total stock quantity across all medications
     */
    public int getTotalStockQuantity() {
        return medicationRepository.findByIsActiveTrue()
                .stream()
                .mapToInt(Medication::getQuantityInStock)
                .sum();
    }

    /**
     * Count how many medications need reordering
     */
    public int countMedicationsNeedingReorder() {
        return medicationRepository.findLowStockMedications().size();
    }

    /**
     * Count expired medications
     */
    public int countExpiredMedications() {
        return medicationRepository.findExpiredMedications().size();
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    private void validateMedicationRequest(MedicationRequest request) {
        // Validate name
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Medication name is required");
        }

        // Validate expiration date
        if (request.getExpirationDate() != null && 
            request.getExpirationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Expiration date must be in the future");
        }

        // Validate quantity
        if (request.getQuantityInStock() != null && request.getQuantityInStock() < 0) {
            throw new RuntimeException("Quantity cannot be negative");
        }

        // Validate reorder level
        if (request.getReorderLevel() != null && request.getReorderLevel() < 0) {
            throw new RuntimeException("Reorder level cannot be negative");
        }
    }

    private void mapRequestToEntity(MedicationRequest request, Medication medication) {
        medication.setName(request.getName());
        medication.setGenericName(request.getGenericName());
        medication.setDosage(request.getDosage());
        medication.setForm(request.getForm());
        medication.setManufacturer(request.getManufacturer());
        medication.setExpirationDate(request.getExpirationDate());
        medication.setQuantityInStock(request.getQuantityInStock());
        medication.setReorderLevel(request.getReorderLevel());
    }
}