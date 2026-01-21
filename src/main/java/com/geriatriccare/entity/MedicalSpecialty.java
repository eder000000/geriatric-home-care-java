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
 * Medical Specialty Entity
 * Healthcare professional specialties
 * 
 * Examples: Geriatrics, Cardiology, Neurology, Internal Medicine
 */
@Entity
@Table(name = "medical_specialties", indexes = {
    @Index(name = "idx_specialty_code", columnList = "code"),
    @Index(name = "idx_specialty_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Specialty code (e.g., GERI for Geriatrics)
     */
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    /**
     * Specialty name
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Parent specialty (for sub-specialties)
     * Example: Geriatric Cardiology -> parent: Cardiology
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_specialty_id")
    private MedicalSpecialty parentSpecialty;

    /**
     * Active flag
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * Typical patient population
     */
    @Size(max = 200, message = "Patient population must not exceed 200 characters")
    @Column(length = 200)
    private String typicalPatientPopulation;

    /**
     * Common conditions treated
     */
    @Column(columnDefinition = "TEXT")
    private String commonConditions;

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
