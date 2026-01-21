package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Diagnosis Entity
 * Medical diagnosis catalog (ICD-10 codes)
 * 
 * Standardizes diagnosis terminology across the system
 */
@Entity
@Table(name = "diagnoses", indexes = {
    @Index(name = "idx_diagnosis_code", columnList = "code"),
    @Index(name = "idx_diagnosis_category", columnList = "category"),
    @Index(name = "idx_diagnosis_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * ICD-10 Code (e.g., I10 for Essential hypertension)
     */
    @NotBlank(message = "ICD-10 code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    /**
     * Diagnosis name
     */
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Detailed description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Category (e.g., Cardiovascular, Respiratory, Neurological)
     */
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(length = 100)
    private String category;

    /**
     * Severity level (Mild, Moderate, Severe)
     */
    @Size(max = 20, message = "Severity must not exceed 20 characters")
    @Column(length = 20)
    private String severity;

    /**
     * Active flag (for deprecating old codes)
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * Common synonyms or alternative names
     */
    @Column(columnDefinition = "TEXT")
    private String synonyms;

    /**
     * Notes for healthcare professionals
     */
    @Column(columnDefinition = "TEXT")
    private String clinicalNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;
}
