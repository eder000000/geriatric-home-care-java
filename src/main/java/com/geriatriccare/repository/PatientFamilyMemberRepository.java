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