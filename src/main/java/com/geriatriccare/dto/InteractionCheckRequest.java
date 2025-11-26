package com.geriatriccare.dto;

import java.util.List;
import java.util.UUID;

public class InteractionCheckRequest {
    private List<UUID> medicationIds;
    private List<String> medicationNames;

    public List<UUID> getMedicationIds() { return medicationIds; }
    public void setMedicationIds(List<UUID> medicationIds) { this.medicationIds = medicationIds; }
    public List<String> getMedicationNames() { return medicationNames; }
    public void setMedicationNames(List<String> medicationNames) { this.medicationNames = medicationNames; }
}
