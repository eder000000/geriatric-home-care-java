package com.geriatriccare.builders;

import java.time.LocalDate;

import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.CarePlanPriority;
import com.geriatriccare.entity.CarePlanStatus;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.CarePlanRepository;

public class CarePlanTestBuilder {

    private String title = "Test Care Plan";
    private String description = "Test Description";
    private Patient patient;
    private User createdBy;
    private User assignedCaregiver;
    private CarePlanStatus status = CarePlanStatus.DRAFT;
    private CarePlanPriority priority = CarePlanPriority.MEDIUM;
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate = LocalDate.now().plusMonths(3);
    
    public static CarePlanTestBuilder aCarePlan() {
        return new CarePlanTestBuilder();
    }
    
    public CarePlanTestBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public CarePlanTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public CarePlanTestBuilder forPatient(Patient patient) {
        this.patient = patient;
        return this;
    }
    
    public CarePlanTestBuilder createdBy(User user) {
        this.createdBy = user;
        return this;
    }
    
    public CarePlanTestBuilder assignedTo(User caregiver) {
        this.assignedCaregiver = caregiver;
        return this;
    }
    
    public CarePlanTestBuilder withStatus(CarePlanStatus status) {
        this.status = status;
        return this;
    }
    
    public CarePlanTestBuilder withPriority(CarePlanPriority priority) {
        this.priority = priority;
        return this;
    }
    
    public CarePlanTestBuilder active() {
        this.status = CarePlanStatus.ACTIVE;
        return this;
    }
    
    public CarePlanTestBuilder highPriority() {
        this.priority = CarePlanPriority.HIGH;
        return this;
    }
    
    public CarePlan build() {
        CarePlan carePlan = new CarePlan();
        carePlan.setTitle(title);
        carePlan.setDescription(description);
        carePlan.setPatient(patient);
        carePlan.setCreatedBy(createdBy);
        carePlan.setAssignedCaregiver(assignedCaregiver);
        carePlan.setStatus(status);
        carePlan.setPriority(priority);
        carePlan.setStartDate(startDate);
        carePlan.setEndDate(endDate);
        return carePlan;
    }
    
    public CarePlan buildAndSave(CarePlanRepository repository) {
        return repository.save(build());
    }
    
}
