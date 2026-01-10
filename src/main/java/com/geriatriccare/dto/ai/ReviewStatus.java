package com.geriatriccare.dto.ai;

public enum ReviewStatus {
    PENDING("Pending Review", "Awaiting healthcare professional review"),
    APPROVED("Approved", "Reviewed and approved for implementation"),
    APPROVED_WITH_MODIFICATIONS("Approved with Modifications", "Approved with changes by reviewer"),
    REJECTED("Rejected", "Not approved - alternative approach needed"),
    IMPLEMENTED("Implemented", "Approved recommendation has been implemented");
    
    private final String displayName;
    private final String description;
    
    ReviewStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
