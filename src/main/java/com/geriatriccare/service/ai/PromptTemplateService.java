package com.geriatriccare.service.ai;

import com.geriatriccare.entity.PromptCategory;
import com.geriatriccare.entity.PromptTemplate;
import com.geriatriccare.repository.PromptTemplateRepository;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);

    private final PromptTemplateRepository promptTemplateRepository;

    public PromptTemplateService(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }

    /**
     * Render a prompt template with variable substitution
     * 
     * @param templateName Name of the template
     * @param variables Map of variable names to values
     * @return Rendered prompt with variables replaced
     */
    @Cacheable(value = "promptTemplates", key = "#templateName")
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        log.debug("Rendering template: {} with {} variables", templateName, variables.size());
        
        PromptTemplate template = promptTemplateRepository.findLatestActiveByName(templateName)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateName));
        
        // Validate required variables
        validateVariables(template, variables);
        
        // Perform variable substitution
        StringSubstitutor substitutor = new StringSubstitutor(variables);
        substitutor.setEnableUndefinedVariableException(true);
        
        String renderedPrompt = substitutor.replace(template.getTemplate());
        
        log.debug("Template rendered successfully: {}", templateName);
        return renderedPrompt;
    }

    /**
     * Get the medical context for a template (used as system message)
     */
    @Cacheable(value = "promptContext", key = "#templateName")
    public String getTemplateContext(String templateName) {
        PromptTemplate template = promptTemplateRepository.findLatestActiveByName(templateName)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateName));
        
        StringBuilder context = new StringBuilder();
        
        if (template.getMedicalContext() != null) {
            context.append(template.getMedicalContext());
        }
        
        if (template.getSafetyGuidelines() != null) {
            context.append("\n\nSafety Guidelines:\n");
            context.append(template.getSafetyGuidelines());
        }
        
        return context.toString();
    }

    /**
     * Create a new prompt template
     */
    @Transactional
    @CacheEvict(value = {"promptTemplates", "promptContext"}, allEntries = true)
    public PromptTemplate createTemplate(PromptTemplate template) {
        log.info("Creating new template: {}", template.getName());
        
        // Check if template name already exists
        if (promptTemplateRepository.existsByName(template.getName())) {
            throw new TemplateAlreadyExistsException("Template already exists: " + template.getName());
        }
        
        template.setVersion(1);
        template.setIsActive(true);
        
        return promptTemplateRepository.save(template);
    }

    /**
     * Create a new version of an existing template
     */
    @Transactional
    @CacheEvict(value = {"promptTemplates", "promptContext"}, allEntries = true)
    public PromptTemplate createNewVersion(String templateName, PromptTemplate updatedTemplate) {
        log.info("Creating new version of template: {}", templateName);
        
        // Get the latest version number
        Integer maxVersion = promptTemplateRepository.findMaxVersionByName(templateName);
        if (maxVersion == null) {
            throw new TemplateNotFoundException("Template not found: " + templateName);
        }
        
        // Deactivate old versions
        List<PromptTemplate> oldVersions = promptTemplateRepository.findByNameOrderByVersionDesc(templateName);
        oldVersions.forEach(t -> t.setIsActive(false));
        promptTemplateRepository.saveAll(oldVersions);
        
        // Create new version
        updatedTemplate.setName(templateName);
        updatedTemplate.setVersion(maxVersion + 1);
        updatedTemplate.setIsActive(true);
        
        return promptTemplateRepository.save(updatedTemplate);
    }

    /**
     * Get a template by name (latest active version)
     */
    public PromptTemplate getTemplate(String templateName) {
        return promptTemplateRepository.findLatestActiveByName(templateName)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateName));
    }

    /**
     * Get all templates by category
     */
    public List<PromptTemplate> getTemplatesByCategory(PromptCategory category) {
        return promptTemplateRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Get all active templates
     */
    public List<PromptTemplate> getAllActiveTemplates() {
        return promptTemplateRepository.findByIsActiveTrueOrderByCategoryAscNameAsc();
    }

    /**
     * Get all versions of a template
     */
    public List<PromptTemplate> getTemplateVersions(String templateName) {
        return promptTemplateRepository.findByNameOrderByVersionDesc(templateName);
    }

    /**
     * Deactivate a template
     */
    @Transactional
    @CacheEvict(value = {"promptTemplates", "promptContext"}, allEntries = true)
    public void deactivateTemplate(String templateName) {
        log.info("Deactivating template: {}", templateName);
        
        List<PromptTemplate> templates = promptTemplateRepository.findByNameOrderByVersionDesc(templateName);
        if (templates.isEmpty()) {
            throw new TemplateNotFoundException("Template not found: " + templateName);
        }
        
        templates.forEach(t -> t.setIsActive(false));
        promptTemplateRepository.saveAll(templates);
    }

    /**
     * Activate a specific version of a template
     */
    @Transactional
    @CacheEvict(value = {"promptTemplates", "promptContext"}, allEntries = true)
    public void activateTemplateVersion(String templateName, Integer version) {
        log.info("Activating template: {} version: {}", templateName, version);
        
        // Deactivate all versions
        List<PromptTemplate> allVersions = promptTemplateRepository.findByNameOrderByVersionDesc(templateName);
        allVersions.forEach(t -> t.setIsActive(false));
        promptTemplateRepository.saveAll(allVersions);
        
        // Activate the specified version
        PromptTemplate template = promptTemplateRepository.findByNameAndVersion(templateName, version)
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Template not found: " + templateName + " version " + version));
        
        template.setIsActive(true);
        promptTemplateRepository.save(template);
    }

    // Helper method to validate required variables
    private void validateVariables(PromptTemplate template, Map<String, Object> variables) {
        if (template.getExpectedVariables() == null || template.getExpectedVariables().isEmpty()) {
            return; // No validation required
        }
        
        String[] expectedVars = template.getExpectedVariables().split(",");
        List<String> missingVars = new ArrayList<>();
        
        for (String varName : expectedVars) {
            String trimmedVar = varName.trim();
            if (!variables.containsKey(trimmedVar)) {
                missingVars.add(trimmedVar);
            }
        }
        
        if (!missingVars.isEmpty()) {
            throw new MissingTemplateVariablesException(
                    "Missing required variables: " + String.join(", ", missingVars));
        }
    }

    // Custom Exceptions
    public static class TemplateNotFoundException extends RuntimeException {
        public TemplateNotFoundException(String message) {
            super(message);
        }
    }

    public static class TemplateAlreadyExistsException extends RuntimeException {
        public TemplateAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class MissingTemplateVariablesException extends RuntimeException {
        public MissingTemplateVariablesException(String message) {
            super(message);
        }
    }
}