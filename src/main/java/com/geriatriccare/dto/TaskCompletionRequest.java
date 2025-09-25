package com.geriatriccare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class TaskCompletionRequest {
    
    @NotNull(message = "Completion timestamp is required")
    private LocalDateTime completedAt;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String completionNotes;
    
    @Size(max = 500, message = "Observations must not exceed 500 characters")
    private String observations;
    
    // Constructors
    public TaskCompletionRequest() {}
    
    public TaskCompletionRequest(LocalDateTime completedAt, String completionNotes, String observations) {
        this.completedAt = completedAt;
        this.completionNotes = completionNotes;
        this.observations = observations;
    }
    
    // Getters and Setters
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}