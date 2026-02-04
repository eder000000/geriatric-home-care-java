package com.geriatriccare.controller;

import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import com.geriatriccare.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);
    
    @Autowired
    private PatientService patientService;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new patient
     * Only OWNER, ADMIN, MANAGER, and CAREGIVER can create patients
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        logger.info("Creating new patient: {} {}", request.getFirstName(), request.getLastName());
        
        try {
            PatientResponse response = patientService.createPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating patient", e);
            throw new RuntimeException("Failed to create patient: " + e.getMessage());
        }
    }
    
    /**
     * Get patient by ID
     * All authenticated users can view patients
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable UUID id) {
        logger.info("Fetching patient: {}", id);
        
        return patientService.getPatientById(id)
                .map(patient -> ResponseEntity.ok(patient))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all patients with pagination
     * OWNER, ADMIN, MANAGER can see all patients
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN')")
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.info("Fetching patients - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                   page, size, sortBy, sortDir);
        
        Page<PatientResponse> patients = patientService.getAllPatients(page, size, sortBy, sortDir);
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Update patient information
     * Only OWNER, ADMIN, MANAGER, and assigned CAREGIVER can update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID id, 
            @Valid @RequestBody PatientRequest request) {
        
        logger.info("Updating patient: {}", id);
        
        try {
            PatientResponse response = patientService.updatePatient(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating patient: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Soft delete patient
     * Only OWNER and ADMIN can delete patients
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        logger.info("Deleting patient: {}", id);
        
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting patient: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== SEARCH OPERATIONS ==========
    
    /**
     * Search patients by name
     */
    @GetMapping("/search/name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<PatientResponse>> searchByName(@RequestParam String name) {
        logger.info("Searching patients by name: {}", name);
        
        List<PatientResponse> patients = patientService.searchPatientsByName(name);
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Search patients by age range
     */
    @GetMapping("/search/age")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<PatientResponse>> searchByAge(
            @RequestParam int minAge, 
            @RequestParam int maxAge) {
        
        logger.info("Searching patients by age range: {} - {}", minAge, maxAge);
        
        List<PatientResponse> patients = patientService.searchPatientsByAgeRange(minAge, maxAge);
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Search patients by medical condition
     */
    @GetMapping("/search/condition")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<PatientResponse>> searchByCondition(@RequestParam String condition) {
        logger.info("Searching patients by medical condition: {}", condition);
        
        List<PatientResponse> patients = patientService.searchPatientsByCondition(condition);
        return ResponseEntity.ok(patients);
    }
    
    // ========== UTILITY ENDPOINTS ==========
    
    /**
     * Get total count of active patients
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN')")
    public ResponseEntity<Long> getTotalPatients() {
        logger.info("Fetching total patient count");
        
        long count = patientService.getTotalActivePatients();
        return ResponseEntity.ok(count);
    }
    
    /**
     * Check if patient exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<Boolean> patientExists(@PathVariable UUID id) {
        logger.info("Checking if patient exists: {}", id);
        
        boolean exists = patientService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}