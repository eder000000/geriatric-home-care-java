package com.geriatriccare.dto.ai;

import com.geriatriccare.entity.PromptCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public class PromptTemplateResponse {
    
    private UUID id;
    private String name;
    private String description;
    private PromptCategory category;
    private String template;
    private String medicalContext;
    private String safetyGuidelines;
    private String expectedVariables;
    private Integer version;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalVersions;
    private Boolean isLatestVersion;
    
    public PromptTemplateResponse() {
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getTotalVersions() {
        return totalVersions;
    }
    
    public void setTotalVersions(Integer totalVersions) {
        this.totalVersions = totalVersions;
    }
    
    public Boolean getIsLatestVersion() {
        return isLatestVersion;
    }
    
    public void setIsLatestVersion(Boolean isLatestVersion) {
        this.isLatestVersion = isLatestVersion;
    }
}