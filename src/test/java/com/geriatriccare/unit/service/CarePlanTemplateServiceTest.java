package com.geriatriccare.unit.service;

import com.geriatriccare.builders.PatientTestBuilder;
import com.geriatriccare.dto.*;
import com.geriatriccare.entity.*;
import com.geriatriccare.repository.*;
import com.geriatriccare.service.CarePlanTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CarePlanTemplateServiceTest {
    
    @Mock
    private CarePlanTemplateRepository templateRepository;
    
    @Mock
    private CarePlanRepository carePlanRepository;
    
    @Mock
    private CareTaskRepository careTaskRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private CarePlanTemplateService templateService;
    
    private CarePlanTemplate testTemplate;
    private CarePlanTemplateRequest templateRequest;
    private Patient testPatient;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("owner@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("Owner");
        testUser.setRole(UserRole.OWNER);
        
        testPatient = PatientTestBuilder.aPatient()
            .withName("John", "Doe")
            .withAge(75)
            .diabetic()
            .build();
        testPatient.setId(UUID.randomUUID());
        
        testTemplate = new CarePlanTemplate();
        testTemplate.setId(UUID.randomUUID());
        testTemplate.setName("Diabetes Care Standard Protocol");
        testTemplate.setDescription("Standard diabetes care protocol");
        testTemplate.setCategory("Chronic Disease Management");
        testTemplate.setCreatedBy(testUser); // CRITICAL: Set createdBy
        
        templateRequest = new CarePlanTemplateRequest();
        templateRequest.setName("Hypertension Protocol");
        templateRequest.setDescription("Standard hypertension care");
        templateRequest.setCategory("Chronic Disease Management");
    }
    
    @Test
    @DisplayName("Should create template successfully")
    void createTemplate_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(templateRepository.save(any(CarePlanTemplate.class))).thenReturn(testTemplate);
        
        CarePlanTemplateResponse response = templateService.createTemplate(templateRequest);
        
        assertThat(response).isNotNull();
        verify(templateRepository).save(any(CarePlanTemplate.class));
    }
    
    @Test
    @DisplayName("Should return template when found by ID")
    void getTemplateById_Found() {
        UUID templateId = testTemplate.getId();
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.of(testTemplate));
        
        Optional<CarePlanTemplateResponse> result = templateService.getTemplateById(templateId);
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Diabetes Care Standard Protocol");
    }
    
    @Test
    @DisplayName("Should return empty when template not found")
    void getTemplateById_NotFound() {
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.empty());
        
        Optional<CarePlanTemplateResponse> result = templateService.getTemplateById(templateId);
        
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should return all active templates")
    void getAllTemplates() {
        CarePlanTemplate template2 = new CarePlanTemplate();
        template2.setId(UUID.randomUUID());
        template2.setName("Hypertension Protocol");
        template2.setCreatedBy(testUser); // Set createdBy
        
        List<CarePlanTemplate> templates = Arrays.asList(testTemplate, template2);
        when(templateRepository.findByIsActiveTrue()).thenReturn(templates);
        
        List<CarePlanTemplateResponse> results = templateService.getAllTemplates();
        
        assertThat(results).hasSize(2);
    }
    
    @Test
    @DisplayName("Should return templates by category")
    void getTemplatesByCategory() {
        String category = "Chronic Disease Management";
        List<CarePlanTemplate> templates = Arrays.asList(testTemplate);
        
        when(templateRepository.findByCategoryAndIsActiveTrue(category))
            .thenReturn(templates);
        
        List<CarePlanTemplateResponse> results = templateService.getTemplatesByCategory(category);
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo(category);
    }
    
    @Test
    @DisplayName("Should update template successfully")
    void updateTemplate_Success() {
        UUID templateId = testTemplate.getId();
        
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(CarePlanTemplate.class)))
            .thenReturn(testTemplate);
        
        CarePlanTemplateResponse response = templateService.updateTemplate(templateId, templateRequest);
        
        assertThat(response).isNotNull();
        verify(templateRepository).save(any(CarePlanTemplate.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent template")
    void updateTemplate_NotFound() {
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> templateService.updateTemplate(templateId, templateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Template not found");
    }
    
    @Test
    @DisplayName("Should deactivate template")
    void deactivateTemplate_Success() {
        UUID templateId = testTemplate.getId();
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.of(testTemplate));
        
        templateService.deactivateTemplate(templateId);
        
        verify(templateRepository).save(testTemplate);
    }
    
    @Test
    @DisplayName("Should throw exception when deactivating non-existent template")
    void deactivateTemplate_NotFound() {
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> templateService.deactivateTemplate(templateId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Template not found");
    }
    
    @Test
    @DisplayName("Should instantiate template into care plan")
    void instantiateTemplate_Success() {
        UUID templateId = testTemplate.getId();
        TemplateInstantiationRequest request = new TemplateInstantiationRequest();
        request.setPatientId(testPatient.getId());
        request.setStartDate(LocalDate.now());
        
        CarePlan createdPlan = new CarePlan();
        createdPlan.setId(UUID.randomUUID());
        createdPlan.setTitle(testTemplate.getName());
        createdPlan.setPatient(testPatient); // CRITICAL: Set patient
        createdPlan.setCreatedBy(testUser);  // CRITICAL: Set createdBy
        
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.of(testTemplate));
        when(patientRepository.findByIdAndIsActiveTrue(testPatient.getId()))
            .thenReturn(Optional.of(testPatient));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(carePlanRepository.save(any(CarePlan.class)))
            .thenReturn(createdPlan);
        
        CarePlanResponse response = templateService.instantiateTemplate(templateId, request);
        
        assertThat(response).isNotNull();
        verify(carePlanRepository).save(any(CarePlan.class));
    }
    
    @Test
    @DisplayName("Should throw exception when instantiating non-existent template")
    void instantiateTemplate_TemplateNotFound() {
        UUID templateId = UUID.randomUUID();
        TemplateInstantiationRequest request = new TemplateInstantiationRequest();
        request.setPatientId(testPatient.getId());
        
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> templateService.instantiateTemplate(templateId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Template not found");
    }
    
    @Test
    @DisplayName("Should throw exception when patient not found during instantiation")
    void instantiateTemplate_PatientNotFound() {
        UUID templateId = testTemplate.getId();
        TemplateInstantiationRequest request = new TemplateInstantiationRequest();
        request.setPatientId(UUID.randomUUID());
        
        when(templateRepository.findByIdAndIsActiveTrue(templateId))
            .thenReturn(Optional.of(testTemplate));
        when(patientRepository.findByIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> templateService.instantiateTemplate(templateId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Patient not found");
    }
}