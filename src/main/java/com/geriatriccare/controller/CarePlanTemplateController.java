package com.geriatriccare.controller;

import com.geriatriccare.dto.CarePlanResponse;
import com.geriatriccare.dto.CarePlanTemplateRequest;
import com.geriatriccare.dto.CarePlanTemplateResponse;
import com.geriatriccare.dto.TemplateInstantiationRequest;
import com.geriatriccare.service.CarePlanTemplateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/care-plan-templates")
@CrossOrigin(origins = "*")
public class CarePlanTemplateController {
    
    private static final Logger logger = LoggerFactory.getLogger(CarePlanTemplateController.class);
    
    @Autowired
    private CarePlanTemplateService templateService;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new template (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<CarePlanTemplateResponse> createTemplate(@Valid @RequestBody CarePlanTemplateRequest request) {
        logger.info("Creating care plan template: {}", request.getName());
        
        try {
            CarePlanTemplateResponse response = templateService.createTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating template", e);
            throw new RuntimeException("Failed to create template: " + e.getMessage());
        }
    }
    
    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<CarePlanTemplateResponse> getTemplate(@PathVariable UUID id) {
        logger.info("Fetching template: {}", id);
        
        return templateService.getTemplateById(id)
                .map(template -> ResponseEntity.ok(template))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all templates
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<CarePlanTemplateResponse>> getAllTemplates() {
        logger.info("Fetching all templates");
        
        List<CarePlanTemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get templates by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<CarePlanTemplateResponse>> getTemplatesByCategory(@PathVariable String category) {
        logger.info("Fetching templates by category: {}", category);
        
        List<CarePlanTemplateResponse> templates = templateService.getTemplatesByCategory(category);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Update template (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<CarePlanTemplateResponse> updateTemplate(
            @PathVariable UUID id, 
            @Valid @RequestBody CarePlanTemplateRequest request) {
        
        logger.info("Updating template: {}", id);
        
        try {
            CarePlanTemplateResponse response = templateService.updateTemplate(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating template: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Deactivate template (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable UUID id) {
        logger.info("Deactivating template: {}", id);
        
        try {
            templateService.deactivateTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deactivating template: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== TEMPLATE INSTANTIATION ==========
    
    /**
     * Create a care plan from a template
     */
    @PostMapping("/{id}/instantiate")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<CarePlanResponse> instantiateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody TemplateInstantiationRequest request) {
        
        logger.info("Instantiating template: {} for patient: {}", id, request.getPatientId());
        
        try {
            CarePlanResponse response = templateService.instantiateTemplate(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error instantiating template: {}", id, e);
            throw new RuntimeException("Failed to instantiate template: " + e.getMessage());
        }
    }
}