package com.geriatriccare.repository;

import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.CarePlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarePlanRepository extends JpaRepository<CarePlan, UUID> {
    
    Optional<CarePlan> findByIdAndIsActiveTrue(UUID id);
    
    Page<CarePlan> findByIsActiveTrue(Pageable pageable);
    
    Page<CarePlan> findByPatientIdAndIsActiveTrue(UUID patientId, Pageable pageable);
    
    Page<CarePlan> findByStatusAndIsActiveTrue(CarePlanStatus status, Pageable pageable);
    
    Page<CarePlan> findByPatientIdAndStatusAndIsActiveTrue(UUID patientId, CarePlanStatus status, Pageable pageable);
    
    List<CarePlan> findByAssignedCaregiverAndIsActiveTrue(com.geriatriccare.entity.User assignedCaregiver);
}