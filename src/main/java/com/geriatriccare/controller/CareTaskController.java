package com.geriatriccare.controller;

import com.geriatriccare.dto.CareTaskRequest;
import com.geriatriccare.dto.CareTaskResponse;
import com.geriatriccare.dto.TaskCompletionRequest;
import com.geriatriccare.service.CareTaskService;
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
@RequestMapping("/api/care-tasks")
@CrossOrigin(origins = "*")
public class CareTaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(CareTaskController.class);
    
    @Autowired
    private CareTaskService careTaskService;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new care task
     * Only OWNER, ADMIN, and CAREGIVER can create tasks
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<CareTaskResponse> createCareTask(@Valid @RequestBody CareTaskRequest request) {
        logger.info("Creating care task: {} for care plan: {}", request.getTaskName(), request.getCarePlanId());
        
        try {
            CareTaskResponse response = careTaskService.createCareTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating care task", e);
            throw new RuntimeException("Failed to create care task: " + e.getMessage());
        }
    }
    
    /**
     * Get care task by ID
     * All authenticated users can view tasks
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<CareTaskResponse> getCareTask(@PathVariable UUID id) {
        logger.info("Fetching care task: {}", id);
        
        return careTaskService.getCareTaskById(id)
                .map(task -> ResponseEntity.ok(task))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all care tasks for a care plan
     */
    @GetMapping("/care-plan/{carePlanId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<List<CareTaskResponse>> getTasksByCarePlan(@PathVariable UUID carePlanId) {
        logger.info("Fetching tasks for care plan: {}", carePlanId);
        
        List<CareTaskResponse> tasks = careTaskService.getTasksByCarePlan(carePlanId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get all care tasks with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Page<CareTaskResponse>> getAllCareTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("Fetching care tasks - page: {}, size: {}", page, size);
        
        Page<CareTaskResponse> tasks = careTaskService.getAllCareTasks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Update care task
     * Only OWNER, ADMIN, and assigned CAREGIVER can update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<CareTaskResponse> updateCareTask(
            @PathVariable UUID id, 
            @Valid @RequestBody CareTaskRequest request) {
        
        logger.info("Updating care task: {}", id);
        
        try {
            CareTaskResponse response = careTaskService.updateCareTask(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating care task: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Soft delete care task
     * Only OWNER and ADMIN can delete tasks
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCareTask(@PathVariable UUID id) {
        logger.info("Deleting care task: {}", id);
        
        try {
            careTaskService.deleteCareTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting care task: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== TASK COMPLETION ==========
    
    /**
     * Mark task as complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<CareTaskResponse> completeTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskCompletionRequest request) {
        
        logger.info("Completing care task: {}", id);
        
        try {
            CareTaskResponse response = careTaskService.completeTask(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error completing care task: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== SEARCH AND FILTER ==========
    
    /**
     * Get overdue tasks
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<CareTaskResponse>> getOverdueTasks() {
        logger.info("Fetching overdue tasks");
        
        List<CareTaskResponse> tasks = careTaskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<CareTaskResponse>> getTasksByCategory(@PathVariable String category) {
        logger.info("Fetching tasks by category: {}", category);
        
        try {
            List<CareTaskResponse> tasks = careTaskService.getTasksByCategory(category);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category: {}", category);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get tasks by priority
     */
    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<CareTaskResponse>> getTasksByPriority(@PathVariable String priority) {
        logger.info("Fetching tasks by priority: {}", priority);
        
        try {
            List<CareTaskResponse> tasks = careTaskService.getTasksByPriority(priority);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid priority: {}", priority);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ========== UTILITY ENDPOINTS ==========
    
    /**
     * Check if care task exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Boolean> taskExists(@PathVariable UUID id) {
        logger.info("Checking if care task exists: {}", id);
        
        boolean exists = careTaskService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}