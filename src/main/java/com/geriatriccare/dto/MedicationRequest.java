package com.geriatriccare.dto;

import com.geriatriccare.entity.MedicationForm;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class MedicationRequest {
    // Required fields: name, dosage, form, expirationDate, quantityInStock, reorderLevel
    @NotBlank
    @Size(max = 255)
    private String name;

    // Optional
    @Size(max = 255)
    private String genericName;

    // Optional
    @Size(max = 255)
    private String manufacturer;

    @NotBlank
    @Size(max = 100)
    private String dosage;

    @NotNull
    private MedicationForm form;

    @NotNull
    @Future
    private LocalDate expirationDate;

    @NotNull
    @Min(0)
    private Integer quantityInStock;

    @NotNull
    @Min(0)
    private Integer reorderLevel;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public MedicationForm getForm() {
        return form;
    }

    public void setForm(MedicationForm form) {
        this.form = form;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
}
