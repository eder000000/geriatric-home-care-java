package com.geriatriccare.dto;

import com.geriatriccare.entity.InteractionSeverity;
import java.util.ArrayList;
import java.util.List;

public class InteractionCheckResponse {
    private boolean hasInteractions;
    private int interactionCount;
    private List<InteractionDetail> interactions;

    public InteractionCheckResponse() {
        this.interactions = new ArrayList<>();
        this.hasInteractions = false;
        this.interactionCount = 0;
    }

    public static class InteractionDetail {
        private String medication1;
        private String medication2;
        private InteractionSeverity severity;
        private String description;
        private String recommendation;

        public String getMedication1() { return medication1; }
        public void setMedication1(String medication1) { this.medication1 = medication1; }
        public String getMedication2() { return medication2; }
        public void setMedication2(String medication2) { this.medication2 = medication2; }
        public InteractionSeverity getSeverity() { return severity; }
        public void setSeverity(InteractionSeverity severity) { this.severity = severity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    public void addInteraction(String med1, String med2, InteractionSeverity severity, String description, String recommendation) {
        InteractionDetail detail = new InteractionDetail();
        detail.setMedication1(med1);
        detail.setMedication2(med2);
        detail.setSeverity(severity);
        detail.setDescription(description);
        detail.setRecommendation(recommendation);
        this.interactions.add(detail);
        this.hasInteractions = true;
        this.interactionCount++;
    }

    public boolean isHasInteractions() { return hasInteractions; }
    public void setHasInteractions(boolean hasInteractions) { this.hasInteractions = hasInteractions; }
    public int getInteractionCount() { return interactionCount; }
    public void setInteractionCount(int interactionCount) { this.interactionCount = interactionCount; }
    public List<InteractionDetail> getInteractions() { return interactions; }
    public void setInteractions(List<InteractionDetail> interactions) { this.interactions = interactions; }
}
