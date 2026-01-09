package com.geriatriccare.dto.ai;

public enum InteractionType {
    DRUG_DRUG("Drug-Drug Interaction", 
            "Interaction between two or more medications"),
    
    DRUG_FOOD("Drug-Food Interaction", 
            "Interaction between medication and food/beverage"),
    
    DRUG_CONDITION("Drug-Disease Interaction", 
            "Medication may worsen existing medical condition"),
    
    DUPLICATE_THERAPY("Duplicate Therapy", 
            "Multiple medications with similar therapeutic effects"),
    
    PHARMACOKINETIC("Pharmacokinetic Interaction", 
            "One drug affects absorption, distribution, metabolism, or excretion of another"),
    
    PHARMACODYNAMIC("Pharmacodynamic Interaction", 
            "Drugs with additive, synergistic, or antagonistic effects");
    
    private final String displayName;
    private final String description;
    
    InteractionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
