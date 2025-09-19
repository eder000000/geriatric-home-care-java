package com.geriatriccare.repository;

import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.PatientFamilyMember;
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
public interface PatientFamilyMemberRepository extends JpaRepository<PatientFamilyMember, UUID> {
    
    // Find by patient
    List<PatientFamilyMember> findByPatientAndIsActiveTrue(Patient patient);
    List<PatientFamilyMember> findByPatientIdAndIsActiveTrue(UUID patientId);
    
    // Find by family member
    List<PatientFamilyMember> findByFamilyMemberAndIsActiveTrue(User familyMember);
    List<PatientFamilyMember> findByFamilyMemberIdAndIsActiveTrue(UUID familyMemberId);
    
    // Check if assignment exists
    boolean existsByPatientIdAndFamilyMemberIdAndIsActiveTrue(UUID patientId, UUID familyMemberId);
    
    // Find specific assignment
    Optional<PatientFamilyMember> findByPatientIdAndFamilyMemberIdAndIsActiveTrue(UUID patientId, UUID familyMemberId);
    
    // Find by assigned by user
    List<PatientFamilyMember> findByAssignedByAndIsActiveTrue(User assignedBy);
    
    // Find assignments within date range
    List<PatientFamilyMember> findByAssignedAtBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count active family members for patient
    @Query("SELECT COUNT(pfm) FROM PatientFamilyMember pfm WHERE pfm.patient = :patient AND pfm.isActive = true")
    long countActiveFamilyMembersByPatient(@Param("patient") Patient patient);
    
    // Count active patients for family member
    @Query("SELECT COUNT(pfm) FROM PatientFamilyMember pfm WHERE pfm.familyMember = :familyMember AND pfm.isActive = true")
    long countActivePatientsForFamilyMember(@Param("familyMember") User familyMember);
    
    // Find patients by family member
    @Query("SELECT DISTINCT pfm.patient FROM PatientFamilyMember pfm WHERE pfm.familyMember = :familyMember AND pfm.isActive = true")
    List<Patient> findPatientsByFamilyMember(@Param("familyMember") User familyMember);
    
    // Find family members by patient
    @Query("SELECT DISTINCT pfm.familyMember FROM PatientFamilyMember pfm WHERE pfm.patient = :patient AND pfm.isActive = true")
    List<User> findFamilyMembersByPatient(@Param("patient") Patient patient);
    
    // Find patients with less than 2 family members (can accept more)
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "(SELECT COUNT(pfm) FROM PatientFamilyMember pfm WHERE pfm.patient = p AND pfm.isActive = true) < 2")
    List<Patient> findPatientsWithLessThanTwoFamilyMembers();
    
    // Find patients with exactly 2 family members (at maximum)
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "(SELECT COUNT(pfm) FROM PatientFamilyMember pfm WHERE pfm.patient = p AND pfm.isActive = true) = 2")
    List<Patient> findPatientsWithMaximumFamilyMembers();
    
    // Find patients without any family members
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "NOT EXISTS (SELECT pfm FROM PatientFamilyMember pfm WHERE pfm.patient = p AND pfm.isActive = true)")
    List<Patient> findPatientsWithoutFamilyMembers();
}