package com.geriatriccare.builders;

import java.time.LocalDate;

import com.geriatriccare.entity.Patient;
import com.geriatriccare.repository.PatientRepository;

public class PatientTestBuilder {

    private String firstName = "John";
    private String lastName = "Doe";
    private LocalDate dateOfBirth = LocalDate.of(1950, 1, 1);
    private String medicalConditions = "None";
    private String emergencyContact = "Jane Doe";
    private String emergencyPhone = "555-9999";
    
    public static PatientTestBuilder aPatient() {
        return new PatientTestBuilder();
    }
    
    public PatientTestBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }
    
    public PatientTestBuilder withAge(int age) {
        this.dateOfBirth = LocalDate.now().minusYears(age);
        return this;
    }
    
    public PatientTestBuilder withMedicalConditions(String conditions) {
        this.medicalConditions = conditions;
        return this;
    }
    
    public PatientTestBuilder diabetic() {
        this.medicalConditions = "Type 2 Diabetes, Hypertension";
        return this;
    }
    
    public PatientTestBuilder withEmergencyContact(String name, String phone) {
        this.emergencyContact = name;
        this.emergencyPhone = phone;
        return this;
    }
    
    public Patient build() {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(dateOfBirth);
        patient.setMedicalConditions(medicalConditions);
        patient.setEmergencyContact(emergencyContact);
        patient.setEmergencyPhone(emergencyPhone);
        return patient;
    }
    
    public Patient buildAndSave(PatientRepository repository) {
        return repository.save(build());
    }

    public PatientTestBuilder withDateOfBirth(LocalDate of) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withDateOfBirth'");
    }
}
