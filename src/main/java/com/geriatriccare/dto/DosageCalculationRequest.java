package com.geriatriccare.dto;

import java.util.UUID;

public class DosageCalculationRequest {
    private UUID medicationId;
    private Integer patientAge;
    private Double patientWeight;
    private Double baseDosage;
    private Double dosagePerKg;
    private Double glomerularFiltrationRate;
    private String childPughScore;
    private Double calculatedDosage;
    private Double maxDailyDose;
    private Integer frequency;

    public UUID getMedicationId() { return medicationId; }
    public void setMedicationId(UUID medicationId) { this.medicationId = medicationId; }
    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }
    public Double getPatientWeight() { return patientWeight; }
    public void setPatientWeight(Double patientWeight) { this.patientWeight = patientWeight; }
    public Double getBaseDosage() { return baseDosage; }
    public void setBaseDosage(Double baseDosage) { this.baseDosage = baseDosage; }
    public Double getDosagePerKg() { return dosagePerKg; }
    public void setDosagePerKg(Double dosagePerKg) { this.dosagePerKg = dosagePerKg; }
    public Double getGlomerularFiltrationRate() { return glomerularFiltrationRate; }
    public void setGlomerularFiltrationRate(Double glomerularFiltrationRate) { this.glomerularFiltrationRate = glomerularFiltrationRate; }
    public String getChildPughScore() { return childPughScore; }
    public void setChildPughScore(String childPughScore) { this.childPughScore = childPughScore; }
    public Double getCalculatedDosage() { return calculatedDosage; }
    public void setCalculatedDosage(Double calculatedDosage) { this.calculatedDosage = calculatedDosage; }
    public Double getMaxDailyDose() { return maxDailyDose; }
    public void setMaxDailyDose(Double maxDailyDose) { this.maxDailyDose = maxDailyDose; }
    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }
}
