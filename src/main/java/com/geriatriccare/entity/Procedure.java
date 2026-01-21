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
 * Procedure Entity
 * Medical procedures catalog (CPT codes)
 * 
 * Standardizes medical procedure terminology
 */
@Entity
@Table(name = "procedures", indexes = {
    @Index(name = "idx_procedure_code", columnList = "code"),
    @Index(name = "idx_procedure_category", columnList = "category"),
    @Index(name = "idx_procedure_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Procedure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * CPT Code (Current Procedural Terminology)
     * Example: 99213 for Office visit
     */
    @NotBlank(message = "CPT code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    /**
     * Procedure name
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
     * Category (e.g., Diagnostic, Therapeutic, Preventive)
     */
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(length = 100)
    private String category;

    /**
     * Estimated duration in minutes
     */
    private Integer estimatedDurationMinutes;

    /**
     * Requires special equipment
     */
    @Column(columnDefinition = "TEXT")
    private String requiredEquipment;

    /**
     * Pre-procedure preparation needed
     */
    @Column(columnDefinition = "TEXT")
    private String preparation;

    /**
     * Post-procedure care instructions
     */
    @Column(columnDefinition = "TEXT")
    private String postCareInstructions;

    /**
     * Active flag
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * Typical specialty performing this procedure
     */
    @Size(max = 100, message = "Specialty must not exceed 100 characters")
    @Column(length = 100)
    private String typicalSpecialty;

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
