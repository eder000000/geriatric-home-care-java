package com.geriatriccare.builders;

import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.CarePlanPriority;
import com.geriatriccare.entity.CarePlanStatus;
import com.geriatriccare.entity.Patient;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BuilderVerificationTest {
    
    @Test
    void patientBuilderWorks() {
        Patient patient = PatientTestBuilder.aPatient()
            .withName("Alice", "Johnson")
            .withAge(75)
            .diabetic()
            .build();
            
        assertThat(patient.getFirstName()).isEqualTo("Alice");
        assertThat(patient.getLastName()).isEqualTo("Johnson");
        assertThat(patient.getMedicalConditions()).contains("Diabetes");
    }
    
    @Test
    void carePlanBuilderWorks() {
        Patient patient = PatientTestBuilder.aPatient().build();
        
        CarePlan carePlan = CarePlanTestBuilder.aCarePlan()
            .withTitle("Diabetes Management")
            .forPatient(patient)
            .highPriority()
            .active()
            .build();
            
        assertThat(carePlan.getTitle()).isEqualTo("Diabetes Management");
        assertThat(carePlan.getStatus()).isEqualTo(CarePlanStatus.ACTIVE);
        assertThat(carePlan.getPriority()).isEqualTo(CarePlanPriority.HIGH);
        assertThat(carePlan.getPatient()).isEqualTo(patient);
    }
    
    @Test
    void builderChainingWorks() {
        Patient patient = PatientTestBuilder.aPatient()
            .withName("Bob", "Smith")
            .withAge(80)
            .withMedicalConditions("Arthritis")
            .withEmergencyContact("Mary Smith", "555-1234")
            .build();
            
        assertThat(patient.getFirstName()).isEqualTo("Bob");
        assertThat(patient.getEmergencyContact()).isEqualTo("Mary Smith");
        assertThat(patient.getEmergencyPhone()).isEqualTo("555-1234");
    }
}
