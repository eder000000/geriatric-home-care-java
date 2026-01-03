package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.PromptTemplateRequest;
import com.geriatriccare.dto.ai.PromptTemplateResponse;
import com.geriatriccare.entity.PromptCategory;
import com.geriatriccare.service.ai.PromptTemplateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {
    
    private static final Logger log = LoggerFactory.getLogger(PromptTemplateController.class);
    
    private final PromptTemplateService templateService;
    
    @Autowired
    public PromptTemplateController(PromptTemplateService templateService) {
        this.templateService = templateService;
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<PromptTemplateResponse> createTemplate(@Valid @RequestBody PromptTemplateRequest request) {
        log.info("Creating prompt template: {}", request.getName());
        PromptTemplateResponse response = templateService.createTemplate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<PromptTemplateResponse> updateTemplate(@PathVariable UUID id, @Valid @RequestBody PromptTemplateRequest request) {
        log.info("Updating prompt template: {}", id);
        PromptTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<PromptTemplateResponse> getTemplate(@PathVariable UUID id) {
        log.info("Getting prompt template: {}", id);
        PromptTemplateResponse response = templateService.getTemplate(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<PromptTemplateResponse> getLatestTemplateByName(@PathVariable String name) {
        log.info("Getting latest template by name: {}", name);
        PromptTemplateResponse response = templateService.getLatestTemplateByName(name);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<List<PromptTemplateResponse>> getAllActiveTemplates() {
        log.info("Getting all active templates");
        List<PromptTemplateResponse> templates = templateService.getAllActiveTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<List<PromptTemplateResponse>> getTemplatesByCategory(@PathVariable PromptCategory category) {
        log.info("Getting templates by category: {}", category);
        List<PromptTemplateResponse> templates = templateService.getTemplatesByCategory(category);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/versions/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<List<PromptTemplateResponse>> getTemplateVersions(@PathVariable String name) {
        log.info("Getting all versions for template: {}", name);
        List<PromptTemplateResponse> versions = templateService.getTemplateVersions(name);
        return ResponseEntity.ok(versions);
    }
    
    @PostMapping("/{id}/render")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<Map<String, String>> renderTemplate(@PathVariable UUID id, @RequestBody Map<String, String> variables) {
        log.info("Rendering template: {} with {} variables", id, variables.size());
        String rendered = templateService.renderTemplate(id, variables);
        
        Map<String, String> response = new HashMap<>();
        response.put("rendered", rendered);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/variables")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<Map<String, List<String>>> extractVariables(@PathVariable UUID id) {
        log.info("Extracting variables from template: {}", id);
        PromptTemplateResponse template = templateService.getTemplate(id);
        List<String> variables = templateService.extractVariables(template.getTemplate());
        
        Map<String, List<String>> response = new HashMap<>();
        response.put("variables", variables);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'CAREGIVER')")
    public ResponseEntity<Map<String, Boolean>> validateTemplate(@PathVariable UUID id, @RequestBody Map<String, String> variables) {
        log.info("Validating template: {} with variables", id);
        boolean isValid = templateService.validateTemplate(id, variables);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        log.info("Deleting template: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}