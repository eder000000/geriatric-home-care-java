package com.geriatriccare.dto;

import java.util.ArrayList;
import java.util.List;

public class DosageCalculationResponse {
    private Double calculatedDosage;
    private String unit;
    private String ageGroup;
    private Boolean renalAdjustmentApplied;
    private Boolean hepaticAdjustmentApplied;
    private List<String> warnings;

    public DosageCalculationResponse() {
        this.warnings = new ArrayList<>();
        this.unit = "mg";
        this.renalAdjustmentApplied = false;
        this.hepaticAdjustmentApplied = false;
    }

    public Double getCalculatedDosage() { return calculatedDosage; }
    public void setCalculatedDosage(Double calculatedDosage) { this.calculatedDosage = calculatedDosage; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public Boolean getRenalAdjustmentApplied() { return renalAdjustmentApplied; }
    public void setRenalAdjustmentApplied(Boolean renalAdjustmentApplied) { 
        this.renalAdjustmentApplied = renalAdjustmentApplied; 
    }

    public Boolean getHepaticAdjustmentApplied() { return hepaticAdjustmentApplied; }
    public void setHepaticAdjustmentApplied(Boolean hepaticAdjustmentApplied) { 
        this.hepaticAdjustmentApplied = hepaticAdjustmentApplied; 
    }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
}
