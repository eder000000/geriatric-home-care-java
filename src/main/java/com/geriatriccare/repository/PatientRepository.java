package com.geriatriccare.repository;

import com.geriatriccare.entity.Patient;
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
    
    // Find by basic properties
    List<Patient> findByIsActiveTrue();
    Optional<Patient> findByIdAndIsActiveTrue(UUID id);
    
    // Find by name (case insensitive)
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Patient> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find by first name
    List<Patient> findByFirstNameContainingIgnoreCaseAndIsActiveTrue(String firstName);
    
    // Find by last name  
    List<Patient> findByLastNameContainingIgnoreCaseAndIsActiveTrue(String lastName);
    
    // Find by date of birth
    List<Patient> findByDateOfBirthAndIsActiveTrue(LocalDate dateOfBirth);
    
    // Find patients by age range
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
    List<Patient> findByAgeBetween(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    // Find patients by emergency contact
    List<Patient> findByEmergencyContactContainingIgnoreCaseAndIsActiveTrue(String emergencyContact);
    
    // Find patients with medical conditions containing text
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "LOWER(p.medicalConditions) LIKE LOWER(CONCAT('%', :condition, '%'))")
    List<Patient> findByMedicalConditionsContaining(@Param("condition") String condition);
    
    // Count all active patients
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isActive = true")
    long countActivePatients();
    
    // Find patients created within date range
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Patient> findPatientsCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                           @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find elderly patients (over certain age)
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) >= :minAge")
    List<Patient> findElderlyPatients(@Param("minAge") int minAge);
}