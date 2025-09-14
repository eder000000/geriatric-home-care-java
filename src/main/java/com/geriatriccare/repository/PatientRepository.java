package com.geriatriccare.repository;

import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    /**
     * Find all active patients
     */
    List<Patient> findByIsActiveTrue();
    
    /**
     * Find patients assigned to a specific caregiver
     */
    List<Patient> findByAssignedCaregiverAndIsActiveTrue(User caregiver);
    
    /**
     * Find patients by caregiver ID
     */
    List<Patient> findByAssignedCaregiver_IdAndIsActiveTrue(UUID caregiverId);
    
    /**
     * Find patients without assigned caregiver
     */
    List<Patient> findByAssignedCaregiverIsNullAndIsActiveTrue();
    
    /**
     * Search patients by name (case insensitive)
     */
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Patient> findByNameContaining(@Param("name") String name);
    
    /**
     * Find patients by age range
     */
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "p.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Patient> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * Count patients by caregiver
     */
    long countByAssignedCaregiverAndIsActiveTrue(User caregiver);
    
    /**
     * Find patients with emergency contact information
     */
    List<Patient> findByEmergencyContactIsNotNullAndIsActiveTrue();
}