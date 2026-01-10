package com.geriatriccare.dto.ai;

public enum ReviewAction {
    APPROVE("Approve", "Approve recommendation as-is"),
    APPROVE_WITH_MODIFICATIONS("Approve with Modifications", "Approve with changes"),
    REJECT("Reject", "Do not approve recommendation"),
    REQUEST_MORE_INFO("Request More Information", "Need additional details"),
    ESCALATE("Escalate", "Escalate to senior provider");
    
    private final String displayName;
    private final String description;
    
    ReviewAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
