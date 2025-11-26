package com.geriatriccare.dto;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MedicationResponse {

    private UUID id;
    private String name;
    private String genericName;
    private String dosage;
    private MedicationForm form;
    private String manufacturer;
    private LocalDate expirationDate;
    private Integer quantityInStock;
    private Integer reorderLevel;
    private Boolean isActive;

    // Calculated fields
    private Boolean isLowStock;
    private Boolean isExpired;
    private Boolean isExpiringSoon;

    // Audit fields
    private LocalDateTime createdAt;
    private String createdByName;
    private LocalDateTime updatedAt;
    private String updatedByName;

    // Static factory method
    public static MedicationResponse fromEntity(Medication medication) {
        MedicationResponse response = new MedicationResponse();

        response.setId(medication.getId());
        response.setName(medication.getName());
        response.setGenericName(medication.getGenericName());
        response.setDosage(medication.getDosage());
        response.setForm(medication.getForm());
        response.setManufacturer(medication.getManufacturer());
        response.setExpirationDate(medication.getExpirationDate());
        response.setQuantityInStock(medication.getQuantityInStock());
        response.setReorderLevel(medication.getReorderLevel());
        response.setIsActive(medication.getIsActive());
        response.setCreatedAt(medication.getCreatedAt());
        response.setUpdatedAt(medication.getUpdatedAt());

        // Calculated fields
        Integer quantity = medication.getQuantityInStock();
        Integer reorder = medication.getReorderLevel();
        response.setIsLowStock(quantity != null && reorder != null && quantity <= reorder);

        LocalDate today = LocalDate.now();
        LocalDate expiration = medication.getExpirationDate();
        response.setIsExpired(expiration != null && expiration.isBefore(today));
        response.setIsExpiringSoon(expiration != null && !response.getIsExpired() && !expiration.isAfter(today.plusDays(30)));

        // Audit fields
        User createdBy = medication.getCreatedBy();
        if (createdBy != null) {
            response.setCreatedByName(createdBy.getFirstName() + " " + createdBy.getLastName());
        }

        User updatedBy = medication.getUpdatedBy();
        if (updatedBy != null) {
            response.setUpdatedByName(updatedBy.getFirstName() + " " + updatedBy.getLastName());
        }

        return response;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public MedicationForm getForm() { return form; }
    public void setForm(MedicationForm form) { this.form = form; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public Integer getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsLowStock() { return isLowStock; }
    public void setIsLowStock(Boolean isLowStock) { this.isLowStock = isLowStock; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public Boolean getIsExpiringSoon() { return isExpiringSoon; }
    public void setIsExpiringSoon(Boolean isExpiringSoon) { this.isExpiringSoon = isExpiringSoon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedByName() { return updatedByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }

    public void setActive(boolean active) { this.isActive = active; }
}
