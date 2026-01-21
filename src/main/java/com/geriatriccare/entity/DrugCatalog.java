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
 * Drug Catalog Entity
 * Standardized medication reference database
 * 
 * Contains generic drug information for medication prescriptions
 */
@Entity
@Table(name = "drug_catalog", indexes = {
    @Index(name = "idx_drug_generic", columnList = "genericName"),
    @Index(name = "idx_drug_category", columnList = "category"),
    @Index(name = "idx_drug_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Generic drug name (e.g., Metformin)
     */
    @NotBlank(message = "Generic name is required")
    @Size(max = 200, message = "Generic name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String genericName;

    /**
     * Common brand names (JSON array)
     * Example: ["Glucophage", "Fortamet", "Glumetza"]
     */
    @Column(columnDefinition = "TEXT")
    private String brandNames;

    /**
     * Drug category/class
     * Example: Antidiabetic, Antihypertensive, Antibiotic
     */
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(length = 100)
    private String category;

    /**
     * Therapeutic class
     * Example: Biguanides, Beta-blockers, Statins
     */
    @Size(max = 100, message = "Therapeutic class must not exceed 100 characters")
    @Column(length = 100)
    private String therapeuticClass;

    /**
     * Available dosage forms (JSON array)
     * Example: ["Tablet", "Extended-release tablet", "Oral solution"]
     */
    @Column(columnDefinition = "TEXT")
    private String dosageForms;

    /**
     * Available strength options (JSON array)
     * Example: ["500mg", "850mg", "1000mg"]
     */
    @Column(columnDefinition = "TEXT")
    private String strengthOptions;

    /**
     * Description and indications
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Common uses/indications
     */
    @Column(columnDefinition = "TEXT")
    private String indications;

    /**
     * Contraindications
     */
    @Column(columnDefinition = "TEXT")
    private String contraindications;

    /**
     * Common side effects
     */
    @Column(columnDefinition = "TEXT")
    private String sideEffects;

    /**
     * Drug interactions (major interactions only)
     */
    @Column(columnDefinition = "TEXT")
    private String interactions;

    /**
     * Special warnings for elderly patients
     */
    @Column(columnDefinition = "TEXT")
    private String geriatricConsiderations;

    /**
     * Active flag
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * FDA approval status
     */
    @Column(length = 50)
    private String fdaStatus;

    /**
     * Controlled substance schedule (if applicable)
     * Example: Schedule II, Schedule III, etc.
     */
    @Column(length = 20)
    private String controlledSubstanceSchedule;

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
