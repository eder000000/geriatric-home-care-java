package com.geriatriccare.service;

import com.geriatriccare.entity.*;
import com.geriatriccare.enums.AuditEventType;
import com.geriatriccare.enums.AuditSeverity;
import com.geriatriccare.dto.security.DataSensitivity;
import com.geriatriccare.repository.*;
import com.geriatriccare.service.security.AuditEnhancementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Catalog Service
 * Business logic for medical catalog management
 * 
 * Manages:
 * - Diagnoses (ICD-10)
 * - Medical Specialties
 * - Procedures (CPT)
 * - Drug Catalog
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final DiagnosisRepository diagnosisRepository;
    private final MedicalSpecialtyRepository medicalSpecialtyRepository;
    private final ProcedureRepository procedureRepository;
    private final DrugCatalogRepository drugCatalogRepository;
    private final AuditEnhancementService auditService;

    // ========== DIAGNOSIS OPERATIONS ==========

    @Transactional
    public Diagnosis createDiagnosis(Diagnosis diagnosis, String createdBy) {
        log.info("Creating diagnosis: {}", diagnosis.getCode());

        if (diagnosisRepository.existsByCode(diagnosis.getCode())) {
            throw new IllegalArgumentException("Diagnosis code already exists: " + diagnosis.getCode());
        }

        diagnosis.setCreatedBy(createdBy);
        diagnosis.setUpdatedBy(createdBy);
        
        Diagnosis saved = diagnosisRepository.save(diagnosis);

        auditService.logEnhancedAuditEvent(
            AuditEventType.CONFIG_CHANGED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            createdBy,
            String.format("Diagnosis created: %s - %s", saved.getCode(), saved.getName()),
            null
        );

        return saved;
    }

    @Transactional
    public Diagnosis updateDiagnosis(UUID id, Diagnosis diagnosis, String updatedBy) {
        log.info("Updating diagnosis: {}", id);

        Diagnosis existing = diagnosisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + id));

        // Check code uniqueness if changed
        if (!existing.getCode().equals(diagnosis.getCode()) && 
            diagnosisRepository.existsByCode(diagnosis.getCode())) {
            throw new IllegalArgumentException("Diagnosis code already exists: " + diagnosis.getCode());
        }

        existing.setCode(diagnosis.getCode());
        existing.setName(diagnosis.getName());
        existing.setDescription(diagnosis.getDescription());
        existing.setCategory(diagnosis.getCategory());
        existing.setSeverity(diagnosis.getSeverity());
        existing.setIsActive(diagnosis.getIsActive());
        existing.setSynonyms(diagnosis.getSynonyms());
        existing.setClinicalNotes(diagnosis.getClinicalNotes());
        existing.setUpdatedBy(updatedBy);

        Diagnosis updated = diagnosisRepository.save(existing);

        auditService.logEnhancedAuditEvent(
            AuditEventType.CONFIG_CHANGED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            updatedBy,
            String.format("Diagnosis updated: %s", updated.getCode()),
            null
        );

        return updated;
    }

    @Transactional(readOnly = true)
    public Diagnosis getDiagnosisById(UUID id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Diagnosis> getAllDiagnoses(int page, int size, boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        
        if (activeOnly) {
            return diagnosisRepository.findByIsActive(true, pageable);
        }
        
        return diagnosisRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Diagnosis> searchDiagnoses(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        return diagnosisRepository.search(query, pageable);
    }

    @Transactional
    public void deleteDiagnosis(UUID id, String deletedBy) {
        log.info("Deleting diagnosis: {}", id);

        Diagnosis diagnosis = getDiagnosisById(id);
        diagnosis.setIsActive(false);
        diagnosis.setUpdatedBy(deletedBy);
        
        diagnosisRepository.save(diagnosis);

        auditService.logEnhancedAuditEvent(
            AuditEventType.CONFIG_CHANGED,
            AuditSeverity.WARNING,
            DataSensitivity.INTERNAL,
            null,
            deletedBy,
            String.format("Diagnosis deactivated: %s", diagnosis.getCode()),
            null
        );
    }

    // ========== MEDICAL SPECIALTY OPERATIONS ==========

    @Transactional
    public MedicalSpecialty createSpecialty(MedicalSpecialty specialty, String createdBy) {
        log.info("Creating medical specialty: {}", specialty.getCode());

        if (medicalSpecialtyRepository.existsByCode(specialty.getCode())) {
            throw new IllegalArgumentException("Specialty code already exists: " + specialty.getCode());
        }

        specialty.setCreatedBy(createdBy);
        specialty.setUpdatedBy(createdBy);
        
        MedicalSpecialty saved = medicalSpecialtyRepository.save(specialty);

        auditService.logEnhancedAuditEvent(
            AuditEventType.CONFIG_CHANGED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            createdBy,
            String.format("Medical specialty created: %s - %s", saved.getCode(), saved.getName()),
            null
        );

        return saved;
    }

    @Transactional
    public MedicalSpecialty updateSpecialty(UUID id, MedicalSpecialty specialty, String updatedBy) {
        log.info("Updating medical specialty: {}", id);

        MedicalSpecialty existing = medicalSpecialtyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medical specialty not found: " + id));

        if (!existing.getCode().equals(specialty.getCode()) && 
            medicalSpecialtyRepository.existsByCode(specialty.getCode())) {
            throw new IllegalArgumentException("Specialty code already exists: " + specialty.getCode());
        }

        existing.setCode(specialty.getCode());
        existing.setName(specialty.getName());
        existing.setDescription(specialty.getDescription());
        existing.setParentSpecialty(specialty.getParentSpecialty());
        existing.setIsActive(specialty.getIsActive());
        existing.setTypicalPatientPopulation(specialty.getTypicalPatientPopulation());
        existing.setCommonConditions(specialty.getCommonConditions());
        existing.setUpdatedBy(updatedBy);

        return medicalSpecialtyRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public MedicalSpecialty getSpecialtyById(UUID id) {
        return medicalSpecialtyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medical specialty not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<MedicalSpecialty> getAllSpecialties(int page, int size, boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        
        if (activeOnly) {
            return medicalSpecialtyRepository.findByIsActive(true, pageable);
        }
        
        return medicalSpecialtyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<MedicalSpecialty> searchSpecialties(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return medicalSpecialtyRepository.search(query, pageable);
    }

    @Transactional
    public void deleteSpecialty(UUID id, String deletedBy) {
        log.info("Deleting medical specialty: {}", id);

        MedicalSpecialty specialty = getSpecialtyById(id);
        specialty.setIsActive(false);
        specialty.setUpdatedBy(deletedBy);
        
        medicalSpecialtyRepository.save(specialty);
    }

    // ========== PROCEDURE OPERATIONS ==========

    @Transactional
    public Procedure createProcedure(Procedure procedure, String createdBy) {
        log.info("Creating procedure: {}", procedure.getCode());

        if (procedureRepository.existsByCode(procedure.getCode())) {
            throw new IllegalArgumentException("Procedure code already exists: " + procedure.getCode());
        }

        procedure.setCreatedBy(createdBy);
        procedure.setUpdatedBy(createdBy);
        
        return procedureRepository.save(procedure);
    }

    @Transactional
    public Procedure updateProcedure(UUID id, Procedure procedure, String updatedBy) {
        log.info("Updating procedure: {}", id);

        Procedure existing = procedureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Procedure not found: " + id));

        if (!existing.getCode().equals(procedure.getCode()) && 
            procedureRepository.existsByCode(procedure.getCode())) {
            throw new IllegalArgumentException("Procedure code already exists: " + procedure.getCode());
        }

        existing.setCode(procedure.getCode());
        existing.setName(procedure.getName());
        existing.setDescription(procedure.getDescription());
        existing.setCategory(procedure.getCategory());
        existing.setEstimatedDurationMinutes(procedure.getEstimatedDurationMinutes());
        existing.setRequiredEquipment(procedure.getRequiredEquipment());
        existing.setPreparation(procedure.getPreparation());
        existing.setPostCareInstructions(procedure.getPostCareInstructions());
        existing.setIsActive(procedure.getIsActive());
        existing.setTypicalSpecialty(procedure.getTypicalSpecialty());
        existing.setUpdatedBy(updatedBy);

        return procedureRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Procedure getProcedureById(UUID id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Procedure not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Procedure> getAllProcedures(int page, int size, boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        
        if (activeOnly) {
            return procedureRepository.findByIsActive(true, pageable);
        }
        
        return procedureRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Procedure> searchProcedures(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        return procedureRepository.search(query, pageable);
    }

    @Transactional
    public void deleteProcedure(UUID id, String deletedBy) {
        log.info("Deleting procedure: {}", id);

        Procedure procedure = getProcedureById(id);
        procedure.setIsActive(false);
        procedure.setUpdatedBy(deletedBy);
        
        procedureRepository.save(procedure);
    }

    // ========== DRUG CATALOG OPERATIONS ==========

    @Transactional
    public DrugCatalog createDrug(DrugCatalog drug, String createdBy) {
        log.info("Creating drug: {}", drug.getGenericName());

        if (drugCatalogRepository.existsByGenericName(drug.getGenericName())) {
            throw new IllegalArgumentException("Drug already exists: " + drug.getGenericName());
        }

        drug.setCreatedBy(createdBy);
        drug.setUpdatedBy(createdBy);
        
        return drugCatalogRepository.save(drug);
    }

    @Transactional
    public DrugCatalog updateDrug(UUID id, DrugCatalog drug, String updatedBy) {
        log.info("Updating drug: {}", id);

        DrugCatalog existing = drugCatalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drug not found: " + id));

        if (!existing.getGenericName().equals(drug.getGenericName()) && 
            drugCatalogRepository.existsByGenericName(drug.getGenericName())) {
            throw new IllegalArgumentException("Drug already exists: " + drug.getGenericName());
        }

        existing.setGenericName(drug.getGenericName());
        existing.setBrandNames(drug.getBrandNames());
        existing.setCategory(drug.getCategory());
        existing.setTherapeuticClass(drug.getTherapeuticClass());
        existing.setDosageForms(drug.getDosageForms());
        existing.setStrengthOptions(drug.getStrengthOptions());
        existing.setDescription(drug.getDescription());
        existing.setIndications(drug.getIndications());
        existing.setContraindications(drug.getContraindications());
        existing.setSideEffects(drug.getSideEffects());
        existing.setInteractions(drug.getInteractions());
        existing.setGeriatricConsiderations(drug.getGeriatricConsiderations());
        existing.setIsActive(drug.getIsActive());
        existing.setFdaStatus(drug.getFdaStatus());
        existing.setControlledSubstanceSchedule(drug.getControlledSubstanceSchedule());
        existing.setUpdatedBy(updatedBy);

        return drugCatalogRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public DrugCatalog getDrugById(UUID id) {
        return drugCatalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drug not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<DrugCatalog> getAllDrugs(int page, int size, boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "genericName"));
        
        if (activeOnly) {
            return drugCatalogRepository.findByIsActive(true, pageable);
        }
        
        return drugCatalogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DrugCatalog> searchDrugs(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "genericName"));
        return drugCatalogRepository.search(query, pageable);
    }

    @Transactional
    public void deleteDrug(UUID id, String deletedBy) {
        log.info("Deleting drug: {}", id);

        DrugCatalog drug = getDrugById(id);
        drug.setIsActive(false);
        drug.setUpdatedBy(deletedBy);
        
        drugCatalogRepository.save(drug);
    }

    // ========== UTILITY METHODS ==========

    @Transactional(readOnly = true)
    public Map<String, Long> getCatalogStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("totalDiagnoses", diagnosisRepository.count());
        stats.put("activeDiagnoses", (long) diagnosisRepository.findAllActive().size());
        stats.put("totalSpecialties", medicalSpecialtyRepository.count());
        stats.put("activeSpecialties", (long) medicalSpecialtyRepository.findAllActive().size());
        stats.put("totalProcedures", procedureRepository.count());
        stats.put("activeProcedures", (long) procedureRepository.findAllActive().size());
        stats.put("totalDrugs", drugCatalogRepository.count());
        stats.put("activeDrugs", (long) drugCatalogRepository.findAllActive().size());
        
        return stats;
    }
}
