package com.geriatriccare.repository;

import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.CarePlanStatus;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarePlanRepository extends JpaRepository<CarePlan, UUID> {
    
    // Find by basic properties
    List<CarePlan> findByIsActiveTrue();
    Page<CarePlan> findByIsActiveTrue(Pageable pageable);
    Optional<CarePlan> findByIdAndIsActiveTrue(UUID id);
    
    // Find by patient
    List<CarePlan> findByPatientAndIsActiveTrue(Patient patient);
    Page<CarePlan> findByPatientIdAndIsActiveTrue(UUID patientId, Pageable pageable);
    
    // Find by status
    List<CarePlan> findByStatusAndIsActiveTrue(CarePlanStatus status);
    Page<CarePlan> findByStatusAndIsActiveTrue(CarePlanStatus status, Pageable pageable);
    
    // Find by patient and status
    List<CarePlan> findByPatientAndStatusAndIsActiveTrue(Patient patient, CarePlanStatus status);
    Page<CarePlan> findByPatientIdAndStatusAndIsActiveTrue(UUID patientId, CarePlanStatus status, Pageable pageable);
    
    // Find by assigned caregiver
    List<CarePlan> findByAssignedCaregiverAndIsActiveTrue(User caregiver);
    Page<CarePlan> findByAssignedCaregiverIdAndIsActiveTrue(UUID caregiverId, Pageable pageable);
    
    // Find by created by user
    List<CarePlan> findByCreatedByUserAndIsActiveTrue(User createdBy);
    
    // Find active care plans (ACTIVE status)
    @Query("SELECT cp FROM CarePlan cp WHERE cp.status = 'ACTIVE' AND cp.isActive = true")
    List<CarePlan> findActiveCarePlans();
    
    // Find care plans by date range
    @Query("SELECT cp FROM CarePlan cp WHERE cp.isActive = true AND " +
           "cp.startDate <= :endDate AND (cp.endDate IS NULL OR cp.endDate >= :startDate)")
    List<CarePlan> findCarePlansByDateRange(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    // Find care plans ending soon
    @Query("SELECT cp FROM CarePlan cp WHERE cp.isActive = true AND cp.status = 'ACTIVE' AND " +
           "cp.endDate IS NOT NULL AND cp.endDate <= :date")
    List<CarePlan> findCarePlansEndingBefore(@Param("date") LocalDate date);
    
    // Find overdue care plans (past end date but still active)
    @Query("SELECT cp FROM CarePlan cp WHERE cp.isActive = true AND cp.status = 'ACTIVE' AND " +
           "cp.endDate IS NOT NULL AND cp.endDate < CURRENT_DATE")
    List<CarePlan> findOverdueCarePlans();
    
    // Count care plans by patient
    @Query("SELECT COUNT(cp) FROM CarePlan cp WHERE cp.patient = :patient AND cp.isActive = true")
    long countActiveCarePlansByPatient(@Param("patient") Patient patient);
    
    // Count care plans by status
    @Query("SELECT COUNT(cp) FROM CarePlan cp WHERE cp.status = :status AND cp.isActive = true")
    long countCarePlansByStatus(@Param("status") CarePlanStatus status);
    
    // Find care plans by priority
    List<CarePlan> findByPriorityAndIsActiveTrueOrderByCreatedAtDesc(com.geriatriccare.entity.CarePlanPriority priority);
}