package com.geriatriccare.service;

import com.geriatriccare.dto.*;
import com.geriatriccare.dto.summary.CaregiverSummary;
import com.geriatriccare.entity.*;
import com.geriatriccare.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarePlanTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CarePlanTemplateService.class);
    
    @Autowired
    private CarePlanTemplateRepository templateRepository;
    
    @Autowired
    private CarePlanRepository carePlanRepository;
    
    @Autowired
    private CareTaskRepository careTaskRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new care plan template
     */
    public CarePlanTemplateResponse createTemplate(CarePlanTemplateRequest request) {
        logger.info("Creating care plan template: {}", request.getName());
        
        User createdBy = getCurrentUser();
        
        CarePlanTemplate template = new CarePlanTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setDefaultPriority(request.getDefaultPriority());
        template.setCategory(request.getCategory());
        template.setCreatedBy(createdBy);
        
        // Add task templates
        for (CarePlanTemplateRequest.CareTaskTemplateRequest taskReq : request.getTaskTemplates()) {
            CareTaskTemplate taskTemplate = new CareTaskTemplate();
            taskTemplate.setTaskName(taskReq.getTaskName());
            taskTemplate.setDescription(taskReq.getDescription());
            taskTemplate.setCategory(taskReq.getCategory());
            taskTemplate.setPriority(taskReq.getPriority());
            taskTemplate.setFrequency(taskReq.getFrequency());
            taskTemplate.setScheduledTime(taskReq.getScheduledTime());
            taskTemplate.setEstimatedDurationMinutes(taskReq.getEstimatedDurationMinutes());
            taskTemplate.setRequiresCaregiverPresence(taskReq.getRequiresCaregiverPresence());
            taskTemplate.setInstructions(taskReq.getInstructions());
            
            template.addTaskTemplate(taskTemplate);
        }
        
        CarePlanTemplate saved = templateRepository.save(template);
        logger.info("Successfully created template with ID: {}", saved.getId());
        
        return convertToResponse(saved);
    }
    
    /**
     * Get template by ID
     */
    @Transactional(readOnly = true)
    public Optional<CarePlanTemplateResponse> getTemplateById(UUID id) {
        logger.debug("Fetching template by ID: {}", id);
        
        return templateRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToResponse);
    }
    
    /**
     * Get all active templates
     */
    @Transactional(readOnly = true)
    public List<CarePlanTemplateResponse> getAllTemplates() {
        logger.debug("Fetching all active templates");
        
        return templateRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get templates by category
     */
    @Transactional(readOnly = true)
    public List<CarePlanTemplateResponse> getTemplatesByCategory(String category) {
        logger.debug("Fetching templates by category: {}", category);
        
        return templateRepository.findByCategoryAndIsActiveTrue(category)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update template
     */
    public CarePlanTemplateResponse updateTemplate(UUID id, CarePlanTemplateRequest request) {
        logger.info("Updating template: {}", id);
        
        CarePlanTemplate template = templateRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setDefaultPriority(request.getDefaultPriority());
        template.setCategory(request.getCategory());
        
        CarePlanTemplate updated = templateRepository.save(template);
        logger.info("Successfully updated template: {}", id);
        
        return convertToResponse(updated);
    }
    
    /**
     * Deactivate template
     */
    public void deactivateTemplate(UUID id) {
        logger.info("Deactivating template: {}", id);
        
        CarePlanTemplate template = templateRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.setIsActive(false);
        templateRepository.save(template);
        
        logger.info("Successfully deactivated template: {}", id);
    }
    
    // ========== TEMPLATE INSTANTIATION ==========
    
    /**
     * Create a care plan from a template
     */
    public CarePlanResponse instantiateTemplate(UUID templateId, TemplateInstantiationRequest request) {
        logger.info("Instantiating template: {} for patient: {}", templateId, request.getPatientId());
        
        CarePlanTemplate template = templateRepository.findByIdAndIsActiveTrue(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        Patient patient = patientRepository.findByIdAndIsActiveTrue(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        User createdBy = getCurrentUser();
        
        // Create care plan from template
        CarePlan carePlan = new CarePlan();
        carePlan.setPatient(patient);
        carePlan.setTitle(template.getName());
        carePlan.setDescription(template.getDescription());
        carePlan.setPriority(template.getDefaultPriority());
        carePlan.setStartDate(request.getStartDate());
        carePlan.setEndDate(request.getEndDate());
        carePlan.setCreatedBy(createdBy);
        carePlan.setStatus(CarePlanStatus.DRAFT);
        
        // Set assigned caregiver if provided
        if (request.getAssignedCaregiverId() != null) {
            User assignedCaregiver = userRepository.findByIdAndIsActiveTrue(request.getAssignedCaregiverId())
                    .orElseThrow(() -> new RuntimeException("Assigned caregiver not found"));
            carePlan.setAssignedCaregiver(assignedCaregiver);
        }
        
        CarePlan savedPlan = carePlanRepository.save(carePlan);
        
        // Create care tasks from template
        for (CareTaskTemplate taskTemplate : template.getTaskTemplates()) {
            CareTask careTask = new CareTask();
            careTask.setCarePlan(savedPlan);
            careTask.setTaskName(taskTemplate.getTaskName());
            careTask.setDescription(taskTemplate.getDescription());
            careTask.setCategory(taskTemplate.getCategory());
            careTask.setPriority(taskTemplate.getPriority());
            careTask.setFrequency(taskTemplate.getFrequency());
            careTask.setScheduledTime(taskTemplate.getScheduledTime());
            careTask.setEstimatedDurationMinutes(taskTemplate.getEstimatedDurationMinutes());
            careTask.setRequiresCaregiverPresence(taskTemplate.getRequiresCaregiverPresence());
            careTask.setInstructions(taskTemplate.getInstructions());
            
            careTaskRepository.save(careTask);
        }
        
        logger.info("Successfully instantiated template {} as care plan {}", templateId, savedPlan.getId());
        
        // Return the created care plan
        return convertCarePlanToResponse(savedPlan);
    }
    
    // ========== UTILITY METHODS ==========
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
    
    // ========== CONVERSION METHODS ==========
    
    private CarePlanTemplateResponse convertToResponse(CarePlanTemplate template) {
        CarePlanTemplateResponse response = new CarePlanTemplateResponse();
        
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setDefaultPriority(template.getDefaultPriority());
        response.setCategory(template.getCategory());
        response.setIsActive(template.getIsActive());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        
        response.setCreatedBy(new CaregiverSummary(
                template.getCreatedBy().getId(),
                template.getCreatedBy().getFirstName(),
                template.getCreatedBy().getLastName(),
                template.getCreatedBy().getEmail(),
                false
        ));
        
        List<CarePlanTemplateResponse.CareTaskTemplateSummary> taskSummaries = template.getTaskTemplates()
                .stream()
                .map(task -> {
                    CarePlanTemplateResponse.CareTaskTemplateSummary summary = 
                            new CarePlanTemplateResponse.CareTaskTemplateSummary();
                    summary.setId(task.getId());
                    summary.setTaskName(task.getTaskName());
                    summary.setCategory(task.getCategory().name());
                    summary.setPriority(task.getPriority().name());
                    summary.setFrequency(task.getFrequency().name());
                    summary.setScheduledTime(task.getScheduledTime());
                    return summary;
                })
                .collect(Collectors.toList());
        
        response.setTaskTemplates(taskSummaries);
        response.setTaskCount(taskSummaries.size());
        
        return response;
    }
    
    private CarePlanResponse convertCarePlanToResponse(CarePlan carePlan) {
        CarePlanResponse response = new CarePlanResponse();
        
        response.setId(carePlan.getId());
        response.setTitle(carePlan.getTitle());
        response.setDescription(carePlan.getDescription());
        response.setPriority(carePlan.getPriority());
        response.setStatus(carePlan.getStatus());
        response.setStartDate(carePlan.getStartDate());
        response.setEndDate(carePlan.getEndDate());
        response.setIsActive(carePlan.getIsActive());
        response.setCreatedAt(carePlan.getCreatedAt());
        response.setUpdatedAt(carePlan.getUpdatedAt());
        response.setPatientId(carePlan.getPatient().getId());
        response.setPatientName(carePlan.getPatient().getFullName());
        
        return response;
    }
}