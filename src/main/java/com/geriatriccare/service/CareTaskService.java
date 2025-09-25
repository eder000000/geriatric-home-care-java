package com.geriatriccare.service;

import com.geriatriccare.dto.CareTaskRequest;
import com.geriatriccare.dto.CareTaskResponse;
import com.geriatriccare.dto.TaskCompletionRequest;
import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.CareTask;
import com.geriatriccare.repository.CarePlanRepository;
import com.geriatriccare.repository.CareTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CareTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(CareTaskService.class);
    
    @Autowired
    private CareTaskRepository careTaskRepository;
    
    @Autowired
    private CarePlanRepository carePlanRepository;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new care task
     */
    public CareTaskResponse createCareTask(CareTaskRequest request) {
        logger.info("Creating care task: {} for care plan: {}", request.getTaskName(), request.getCarePlanId());
        
        // Validate care plan exists
        CarePlan carePlan = carePlanRepository.findByIdAndIsActiveTrue(request.getCarePlanId())
                .orElseThrow(() -> new RuntimeException("Care plan not found"));
        
        // Create care task
        CareTask careTask = new CareTask();
        careTask.setCarePlan(carePlan);
        careTask.setTaskName(request.getTaskName());
        careTask.setDescription(request.getDescription());
        careTask.setCategory(request.getCategory());
        careTask.setPriority(request.getPriority());
        careTask.setFrequency(request.getFrequency());
        careTask.setScheduledTime(request.getScheduledTime());
        careTask.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        careTask.setRequiresCaregiverPresence(request.getRequiresCaregiverPresence() != null ? 
                request.getRequiresCaregiverPresence() : false);
        careTask.setInstructions(request.getInstructions());
        
        CareTask saved = careTaskRepository.save(careTask);
        logger.info("Successfully created care task with ID: {}", saved.getId());
        
        return convertToResponse(saved);
    }
    
    /**
     * Get care task by ID
     */
    @Transactional(readOnly = true)
    public Optional<CareTaskResponse> getCareTaskById(UUID id) {
        logger.debug("Fetching care task by ID: {}", id);
        
        return careTaskRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToResponse);
    }
    
    /**
     * Get all care tasks for a care plan
     */
    @Transactional(readOnly = true)
    public List<CareTaskResponse> getTasksByCarePlan(UUID carePlanId) {
        logger.debug("Fetching tasks for care plan: {}", carePlanId);
        
        return careTaskRepository.findByCarePlanIdAndIsActiveTrue(carePlanId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all care tasks with pagination
     */
    @Transactional(readOnly = true)
    public Page<CareTaskResponse> getAllCareTasks(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching care tasks - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return careTaskRepository.findByIsActiveTrue(pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Update care task
     */
    public CareTaskResponse updateCareTask(UUID id, CareTaskRequest request) {
        logger.info("Updating care task: {}", id);
        
        CareTask careTask = careTaskRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care task not found"));
        
        // Update fields
        careTask.setTaskName(request.getTaskName());
        careTask.setDescription(request.getDescription());
        careTask.setCategory(request.getCategory());
        careTask.setPriority(request.getPriority());
        careTask.setFrequency(request.getFrequency());
        careTask.setScheduledTime(request.getScheduledTime());
        careTask.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        careTask.setRequiresCaregiverPresence(request.getRequiresCaregiverPresence());
        careTask.setInstructions(request.getInstructions());
        
        CareTask updated = careTaskRepository.save(careTask);
        logger.info("Successfully updated care task: {}", id);
        
        return convertToResponse(updated);
    }
    
    /**
     * Soft delete care task
     */
    public void deleteCareTask(UUID id) {
        logger.info("Soft deleting care task: {}", id);
        
        CareTask careTask = careTaskRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care task not found"));
        
        careTask.setIsActive(false);
        careTaskRepository.save(careTask);
        
        logger.info("Successfully soft deleted care task: {}", id);
    }
    
    // ========== TASK COMPLETION ==========
    
    /**
     * Mark task as complete
     */
    public CareTaskResponse completeTask(UUID id, TaskCompletionRequest request) {
        logger.info("Completing care task: {}", id);
        
        CareTask careTask = careTaskRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care task not found"));
        
        // For now, we'll just log completion (full implementation would store completion history)
        logger.info("Task completed at: {} with notes: {}", 
                   request.getCompletedAt(), request.getCompletionNotes());
        
        // Update task (in future sprint, this would link to a TaskCompletion entity)
        careTask.setUpdatedAt(LocalDateTime.now());
        CareTask updated = careTaskRepository.save(careTask);
        
        return convertToResponse(updated);
    }
    
    // ========== SEARCH AND FILTER ==========
    
    /**
     * Get overdue tasks
     */
    @Transactional(readOnly = true)
    public List<CareTaskResponse> getOverdueTasks() {
        logger.debug("Fetching overdue tasks");
        
        // Basic implementation - can be enhanced with completion tracking
        return careTaskRepository.findByIsActiveTrue(PageRequest.of(0, 100))
                .stream()
                .map(this::convertToResponse)
                .filter(task -> task.getIsOverdue() != null && task.getIsOverdue())
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by category
     */
    @Transactional(readOnly = true)
    public List<CareTaskResponse> getTasksByCategory(String category) {
        logger.debug("Fetching tasks by category: {}", category);
        
        return careTaskRepository.findByCategoryAndIsActiveTrue(
                com.geriatriccare.entity.CareTaskCategory.valueOf(category))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by priority
     */
    @Transactional(readOnly = true)
    public List<CareTaskResponse> getTasksByPriority(String priority) {
        logger.debug("Fetching tasks by priority: {}", priority);
        
        return careTaskRepository.findByPriorityAndIsActiveTrue(
                com.geriatriccare.entity.CareTaskPriority.valueOf(priority))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Check if care task exists and is active
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return careTaskRepository.findByIdAndIsActiveTrue(id).isPresent();
    }
    
    // ========== CONVERSION METHODS ==========
    
    /**
     * Convert CareTask entity to CareTaskResponse DTO
     */
    private CareTaskResponse convertToResponse(CareTask careTask) {
        CareTaskResponse response = new CareTaskResponse();
        
        // Basic task information
        response.setId(careTask.getId());
        response.setCarePlanId(careTask.getCarePlan().getId());
        response.setCarePlanTitle(careTask.getCarePlan().getTitle());
        response.setTaskName(careTask.getTaskName());
        response.setDescription(careTask.getDescription());
        response.setCategory(careTask.getCategory());
        response.setPriority(careTask.getPriority());
        response.setFrequency(careTask.getFrequency());
        response.setScheduledTime(careTask.getScheduledTime());
        response.setEstimatedDurationMinutes(careTask.getEstimatedDurationMinutes());
        response.setRequiresCaregiverPresence(careTask.getRequiresCaregiverPresence());
        response.setInstructions(careTask.getInstructions());
        response.setIsActive(careTask.getIsActive());
        response.setCreatedAt(careTask.getCreatedAt());
        response.setUpdatedAt(careTask.getUpdatedAt());
        
        // Completion tracking (placeholder - will be enhanced in future sprint)
        response.setIsCompleted(false);
        response.setLastCompletedAt(null);
        response.setNextDueDate(null);
        response.setIsOverdue(false);
        
        return response;
    }
}