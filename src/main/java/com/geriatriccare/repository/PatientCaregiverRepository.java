package com.geriatriccare.repository;

import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.PatientCaregiver;
import com.geriatriccare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientCaregiverRepository extends JpaRepository<PatientCaregiver, PatientCaregiver.PatientCaregiverId> {
    
    // Find by patient
    List<PatientCaregiver> findByPatientAndIsActiveTrue(Patient patient);
    List<PatientCaregiver> findByPatientIdAndIsActiveTrue(UUID patientId);
    
    // Find by caregiver
    List<PatientCaregiver> findByCaregiverAndIsActiveTrue(User caregiver);
    List<PatientCaregiver> findByCaregiverIdAndIsActiveTrue(UUID caregiverId);
    
    // Find primary caregiver for patient
    Optional<PatientCaregiver> findByPatientAndIsPrimaryTrueAndIsActiveTrue(Patient patient);
    Optional<PatientCaregiver> findByPatientIdAndIsPrimaryTrueAndIsActiveTrue(UUID patientId);
    
    // Check if assignment exists
    boolean existsByPatientIdAndCaregiverIdAndIsActiveTrue(UUID patientId, UUID caregiverId);
    
    // Find specific assignment
    Optional<PatientCaregiver> findByPatientIdAndCaregiverIdAndIsActiveTrue(UUID patientId, UUID caregiverId);
    
    // Find by assigned by user
    List<PatientCaregiver> findByAssignedByAndIsActiveTrue(User assignedBy);
    
    // Find assignments within date range
    List<PatientCaregiver> findByAssignedAtBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count active caregivers for patient
    @Query("SELECT COUNT(pc) FROM PatientCaregiver pc WHERE pc.patient = :patient AND pc.isActive = true")
    long countActiveCaregiversByPatient(@Param("patient") Patient patient);
    
    // Count active patients for caregiver
    @Query("SELECT COUNT(pc) FROM PatientCaregiver pc WHERE pc.caregiver = :caregiver AND pc.isActive = true")
    long countActivePatientsForCaregiver(@Param("caregiver") User caregiver);
    
    // Find all primary caregivers
    @Query("SELECT pc FROM PatientCaregiver pc WHERE pc.isPrimary = true AND pc.isActive = true")
    List<PatientCaregiver> findAllPrimaryCaregivers();
    
    // Find patients without primary caregiver
    @Query("SELECT DISTINCT p FROM Patient p WHERE p.isActive = true AND " +
           "NOT EXISTS (SELECT pc FROM PatientCaregiver pc WHERE pc.patient = p AND pc.isPrimary = true AND pc.isActive = true)")
    List<Patient> findPatientsWithoutPrimaryCaregiver();
    
    // Find patients by caregiver
    @Query("SELECT DISTINCT pc.patient FROM PatientCaregiver pc WHERE pc.caregiver = :caregiver AND pc.isActive = true")
    List<Patient> findPatientsByCaregiver(@Param("caregiver") User caregiver);
    
    // Find caregivers by patient
    @Query("SELECT DISTINCT pc.caregiver FROM PatientCaregiver pc WHERE pc.patient = :patient AND pc.isActive = true")
    List<User> findCaregiversByPatient(@Param("patient") Patient patient);
}