package com.geriatriccare.dto.ai;

import com.geriatriccare.entity.PromptCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PromptTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Template name must not exceed 255 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    private PromptCategory category;
    
    @NotBlank(message = "Template content is required")
    private String template;
    
    private String medicalContext;
    private String safetyGuidelines;
    private String expectedVariables;
    private Integer version;
    private Boolean isActive = true;
    
    public PromptTemplateRequest() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public PromptCategory getCategory() {
        return category;
    }
    
    public void setCategory(PromptCategory category) {
        this.category = category;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getMedicalContext() {
        return medicalContext;
    }
    
    public void setMedicalContext(String medicalContext) {
        this.medicalContext = medicalContext;
    }
    
    public String getSafetyGuidelines() {
        return safetyGuidelines;
    }
    
    public void setSafetyGuidelines(String safetyGuidelines) {
        this.safetyGuidelines = safetyGuidelines;
    }
    
    public String getExpectedVariables() {
        return expectedVariables;
    }
    
    public void setExpectedVariables(String expectedVariables) {
        this.expectedVariables = expectedVariables;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}