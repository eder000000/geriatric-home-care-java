package com.geriatriccare.service.ai;

import com.geriatriccare.dto.ai.PromptTemplateRequest;
import com.geriatriccare.dto.ai.PromptTemplateResponse;
import com.geriatriccare.entity.PromptCategory;
import com.geriatriccare.entity.PromptTemplate;
import com.geriatriccare.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PromptTemplateService {
    
    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    private final PromptTemplateRepository templateRepository;
    
    @Autowired
    public PromptTemplateService(PromptTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
    
    @Transactional
    public PromptTemplateResponse createTemplate(PromptTemplateRequest request) {
        log.info("Creating new prompt template: {}", request.getName());
        
        if (templateRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Template with name '" + request.getName() + "' already exists");
        }
        
        PromptTemplate template = buildTemplateEntity(request);
        template.setVersion(1);
        
        PromptTemplate saved = templateRepository.save(template);
        log.info("Created template: {} (ID: {})", saved.getName(), saved.getId());
        
        return convertToResponse(saved);
    }
    
    @Transactional
    public PromptTemplateResponse updateTemplate(UUID id, PromptTemplateRequest request) {
        log.info("Updating template: {}", id);
        
        PromptTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        
        existingTemplate.setIsActive(false);
        templateRepository.save(existingTemplate);
        
        Integer maxVersion = templateRepository.findMaxVersionByName(existingTemplate.getName());
        Integer newVersion = (maxVersion != null ? maxVersion : 0) + 1;
        
        PromptTemplate newTemplate = buildTemplateEntity(request);
        newTemplate.setName(existingTemplate.getName());
        newTemplate.setVersion(newVersion);
        
        PromptTemplate saved = templateRepository.save(newTemplate);
        log.info("Created new version {} for template: {}", newVersion, existingTemplate.getName());
        
        return convertToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public PromptTemplateResponse getTemplate(UUID id) {
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        
        return convertToResponse(template);
    }
    
    @Transactional(readOnly = true)
    public PromptTemplateResponse getLatestTemplateByName(String name) {
        List<PromptTemplate> versions = templateRepository.findByNameOrderByVersionDesc(name);
        
        if (versions.isEmpty()) {
            throw new RuntimeException("Template not found: " + name);
        }
        
        PromptTemplate latest = versions.stream()
                .filter(PromptTemplate::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active version found for template: " + name));
        
        return convertToResponse(latest);
    }
    
    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrueOrderByCategoryAscNameAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getTemplatesByCategory(PromptCategory category) {
        return templateRepository.findByCategoryOrderByNameAscVersionDesc(category)
                .stream()
                .filter(PromptTemplate::getIsActive)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getTemplateVersions(String name) {
        return templateRepository.findByNameOrderByVersionDesc(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deactivateTemplate(UUID id) {
        log.info("Deactivating template: {}", id);
        
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        
        template.setIsActive(false);
        templateRepository.save(template);
        
        log.info("Deactivated template: {}", template.getName());
    }
    
    @Transactional
    public void deleteTemplate(UUID id) {
        deactivateTemplate(id);
    }
    
    public String renderTemplate(UUID templateId, Map<String, String> variables) {
        PromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
        
        return renderTemplate(template.getTemplate(), variables);
    }
    
    public String renderTemplate(String templateString, Map<String, String> variables) {
        if (templateString == null || variables == null) {
            return templateString;
        }
        
        String result = templateString;
        Matcher matcher = VARIABLE_PATTERN.matcher(templateString);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variables.get(variableName);
            
            if (value != null) {
                result = result.replace("${" + variableName + "}", value);
            } else {
                log.warn("Missing variable in template rendering: {}", variableName);
            }
        }
        
        return result;
    }
    
    public List<String> extractVariables(String templateString) {
        if (templateString == null) {
            return List.of();
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(templateString);
        return matcher.results()
                .map(matchResult -> matchResult.group(1))
                .distinct()
                .collect(Collectors.toList());
    }
    
    public boolean validateTemplate(UUID templateId, Map<String, String> variables) {
        PromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
        
        List<String> requiredVariables = extractVariables(template.getTemplate());
        
        return requiredVariables.stream()
                .allMatch(variables::containsKey);
    }
    
    private PromptTemplate buildTemplateEntity(PromptTemplateRequest request) {
        PromptTemplate template = new PromptTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setTemplate(request.getTemplate());
        template.setMedicalContext(request.getMedicalContext());
        template.setSafetyGuidelines(request.getSafetyGuidelines());
        template.setExpectedVariables(request.getExpectedVariables());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return template;
    }
    
    private PromptTemplateResponse convertToResponse(PromptTemplate template) {
        List<PromptTemplate> allVersions = templateRepository.findByNameOrderByVersionDesc(template.getName());
        Integer totalVersions = allVersions.size();
        Integer latestVersion = allVersions.isEmpty() ? 1 : allVersions.get(0).getVersion();
        
        PromptTemplateResponse response = new PromptTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setCategory(template.getCategory());
        response.setTemplate(template.getTemplate());
        response.setMedicalContext(template.getMedicalContext());
        response.setSafetyGuidelines(template.getSafetyGuidelines());
        response.setExpectedVariables(template.getExpectedVariables());
        response.setVersion(template.getVersion());
        response.setIsActive(template.getIsActive());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        response.setTotalVersions(totalVersions);
        response.setIsLatestVersion(template.getVersion().equals(latestVersion));
        
        return response;
    }
}