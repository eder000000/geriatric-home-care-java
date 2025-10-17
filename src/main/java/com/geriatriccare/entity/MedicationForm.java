package com.geriatriccare.entity;

public enum MedicationForm {
    TABLET("Tablet"),
    CAPSULE("Capsule"),
    LIQUID("Liquid"),
    INJECTION("Injection"),
    TOPICAL("Topical");

    private final String displayName;

    MedicationForm(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
 
}
