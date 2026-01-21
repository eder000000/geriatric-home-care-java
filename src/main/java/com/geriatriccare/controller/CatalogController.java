package com.geriatriccare.controller;

import com.geriatriccare.entity.*;
import com.geriatriccare.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Catalog Management Controller
 * REST API for medical catalogs
 * 
 * Endpoints (16):
 * Diagnoses (4):
 * - GET /api/catalogs/diagnoses
 * - POST /api/catalogs/diagnoses
 * - PUT /api/catalogs/diagnoses/{id}
 * - DELETE /api/catalogs/diagnoses/{id}
 * 
 * Medical Specialties (4):
 * - GET /api/catalogs/specialties
 * - POST /api/catalogs/specialties
 * - PUT /api/catalogs/specialties/{id}
 * - DELETE /api/catalogs/specialties/{id}
 * 
 * Procedures (4):
 * - GET /api/catalogs/procedures
 * - POST /api/catalogs/procedures
 * - PUT /api/catalogs/procedures/{id}
 * - DELETE /api/catalogs/procedures/{id}
 * 
 * Drugs (4):
 * - GET /api/catalogs/drugs
 * - POST /api/catalogs/drugs
 * - PUT /api/catalogs/drugs/{id}
 * - DELETE /api/catalogs/drugs/{id}
 */
@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Medical Catalogs", description = "Medical catalog management operations")
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

    private final CatalogService catalogService;

    // ========== DIAGNOSIS ENDPOINTS ==========

    @GetMapping("/diagnoses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all diagnoses", description = "Get paginated list of diagnoses (ICD-10)")
    public ResponseEntity<Page<Diagnosis>> getAllDiagnoses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String search) {
        
        log.info("Getting diagnoses - page: {}, size: {}, search: {}", page, size, search);
        
        Page<Diagnosis> diagnoses;
        if (search != null && !search.trim().isEmpty()) {
            diagnoses = catalogService.searchDiagnoses(search, page, size);
        } else {
            diagnoses = catalogService.getAllDiagnoses(page, size, activeOnly);
        }
        
        return ResponseEntity.ok(diagnoses);
    }

    @PostMapping("/diagnoses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create diagnosis", description = "Create new diagnosis (ADMIN only)")
    public ResponseEntity<Diagnosis> createDiagnosis(
            @Valid @RequestBody Diagnosis diagnosis,
            Authentication authentication) {
        
        log.info("Creating diagnosis: {}", diagnosis.getCode());
        
        Diagnosis created = catalogService.createDiagnosis(diagnosis, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/diagnoses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update diagnosis", description = "Update existing diagnosis (ADMIN only)")
    public ResponseEntity<Diagnosis> updateDiagnosis(
            @PathVariable UUID id,
            @Valid @RequestBody Diagnosis diagnosis,
            Authentication authentication) {
        
        log.info("Updating diagnosis: {}", id);
        
        Diagnosis updated = catalogService.updateDiagnosis(id, diagnosis, authentication.getName());
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/diagnoses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete diagnosis", description = "Deactivate diagnosis (ADMIN only)")
    public ResponseEntity<Map<String, String>> deleteDiagnosis(
            @PathVariable UUID id,
            Authentication authentication) {
        
        log.info("Deleting diagnosis: {}", id);
        
        catalogService.deleteDiagnosis(id, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Diagnosis deactivated successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    // ========== MEDICAL SPECIALTY ENDPOINTS ==========

    @GetMapping("/specialties")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all specialties", description = "Get paginated list of medical specialties")
    public ResponseEntity<Page<MedicalSpecialty>> getAllSpecialties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String search) {
        
        log.info("Getting specialties - page: {}, size: {}, search: {}", page, size, search);
        
        Page<MedicalSpecialty> specialties;
        if (search != null && !search.trim().isEmpty()) {
            specialties = catalogService.searchSpecialties(search, page, size);
        } else {
            specialties = catalogService.getAllSpecialties(page, size, activeOnly);
        }
        
        return ResponseEntity.ok(specialties);
    }

    @PostMapping("/specialties")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create specialty", description = "Create new medical specialty (ADMIN only)")
    public ResponseEntity<MedicalSpecialty> createSpecialty(
            @Valid @RequestBody MedicalSpecialty specialty,
            Authentication authentication) {
        
        log.info("Creating specialty: {}", specialty.getCode());
        
        MedicalSpecialty created = catalogService.createSpecialty(specialty, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/specialties/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update specialty", description = "Update existing specialty (ADMIN only)")
    public ResponseEntity<MedicalSpecialty> updateSpecialty(
            @PathVariable UUID id,
            @Valid @RequestBody MedicalSpecialty specialty,
            Authentication authentication) {
        
        log.info("Updating specialty: {}", id);
        
        MedicalSpecialty updated = catalogService.updateSpecialty(id, specialty, authentication.getName());
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/specialties/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete specialty", description = "Deactivate specialty (ADMIN only)")
    public ResponseEntity<Map<String, String>> deleteSpecialty(
            @PathVariable UUID id,
            Authentication authentication) {
        
        log.info("Deleting specialty: {}", id);
        
        catalogService.deleteSpecialty(id, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Specialty deactivated successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    // ========== PROCEDURE ENDPOINTS ==========

    @GetMapping("/procedures")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all procedures", description = "Get paginated list of procedures (CPT)")
    public ResponseEntity<Page<Procedure>> getAllProcedures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String search) {
        
        log.info("Getting procedures - page: {}, size: {}, search: {}", page, size, search);
        
        Page<Procedure> procedures;
        if (search != null && !search.trim().isEmpty()) {
            procedures = catalogService.searchProcedures(search, page, size);
        } else {
            procedures = catalogService.getAllProcedures(page, size, activeOnly);
        }
        
        return ResponseEntity.ok(procedures);
    }

    @PostMapping("/procedures")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create procedure", description = "Create new procedure (ADMIN only)")
    public ResponseEntity<Procedure> createProcedure(
            @Valid @RequestBody Procedure procedure,
            Authentication authentication) {
        
        log.info("Creating procedure: {}", procedure.getCode());
        
        Procedure created = catalogService.createProcedure(procedure, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/procedures/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update procedure", description = "Update existing procedure (ADMIN only)")
    public ResponseEntity<Procedure> updateProcedure(
            @PathVariable UUID id,
            @Valid @RequestBody Procedure procedure,
            Authentication authentication) {
        
        log.info("Updating procedure: {}", id);
        
        Procedure updated = catalogService.updateProcedure(id, procedure, authentication.getName());
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/procedures/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete procedure", description = "Deactivate procedure (ADMIN only)")
    public ResponseEntity<Map<String, String>> deleteProcedure(
            @PathVariable UUID id,
            Authentication authentication) {
        
        log.info("Deleting procedure: {}", id);
        
        catalogService.deleteProcedure(id, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Procedure deactivated successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    // ========== DRUG CATALOG ENDPOINTS ==========

    @GetMapping("/drugs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all drugs", description = "Get paginated list of drugs")
    public ResponseEntity<Page<DrugCatalog>> getAllDrugs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String search) {
        
        log.info("Getting drugs - page: {}, size: {}, search: {}", page, size, search);
        
        Page<DrugCatalog> drugs;
        if (search != null && !search.trim().isEmpty()) {
            drugs = catalogService.searchDrugs(search, page, size);
        } else {
            drugs = catalogService.getAllDrugs(page, size, activeOnly);
        }
        
        return ResponseEntity.ok(drugs);
    }

    @PostMapping("/drugs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create drug", description = "Create new drug catalog entry (ADMIN only)")
    public ResponseEntity<DrugCatalog> createDrug(
            @Valid @RequestBody DrugCatalog drug,
            Authentication authentication) {
        
        log.info("Creating drug: {}", drug.getGenericName());
        
        DrugCatalog created = catalogService.createDrug(drug, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/drugs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update drug", description = "Update existing drug (ADMIN only)")
    public ResponseEntity<DrugCatalog> updateDrug(
            @PathVariable UUID id,
            @Valid @RequestBody DrugCatalog drug,
            Authentication authentication) {
        
        log.info("Updating drug: {}", id);
        
        DrugCatalog updated = catalogService.updateDrug(id, drug, authentication.getName());
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/drugs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete drug", description = "Deactivate drug (ADMIN only)")
    public ResponseEntity<Map<String, String>> deleteDrug(
            @PathVariable UUID id,
            Authentication authentication) {
        
        log.info("Deleting drug: {}", id);
        
        catalogService.deleteDrug(id, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Drug deactivated successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    // ========== STATISTICS & HEALTH ==========

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get catalog statistics", description = "Get statistics for all catalogs (ADMIN only)")
    public ResponseEntity<Map<String, Long>> getCatalogStatistics() {
        log.info("Getting catalog statistics");
        
        Map<String, Long> stats = catalogService.getCatalogStatistics();
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Catalog service health check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Medical Catalog Service");
        health.put("catalogs", Map.of(
            "diagnoses", "ICD-10",
            "specialties", "Medical Specialties",
            "procedures", "CPT Codes",
            "drugs", "Drug Catalog"
        ));
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}
