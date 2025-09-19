package com.geriatriccare.service;

import com.geriatriccare.entity.*;
import com.geriatriccare.repository.PatientCaregiverRepository;
import com.geriatriccare.repository.PatientFamilyMemberRepository;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PatientRelationshipService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientRelationshipService.class);
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PatientCaregiverRepository patientCaregiverRepository;
    
    @Autowired
    private PatientFamilyMemberRepository patientFamilyMemberRepository;
    
    // ========== CAREGIVER ASSIGNMENT METHODS ==========
    
    /**
     * Assign a caregiver to a patient
     */
    public PatientCaregiver assignCaregiver(UUID patientId, UUID caregiverId, UUID assignedById, boolean isPrimary) {
        logger.info("Assigning caregiver {} to patient {} (primary: {})", caregiverId, patientId, isPrimary);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));
        
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigning user not found"));
        
        // Validate caregiver role
        if (caregiver.getRole() != UserRole.CAREGIVER) {
            throw new RuntimeException("User must have CAREGIVER role to be assigned as caregiver");
        }
        
        // Check if assignment already exists
        if (patientCaregiverRepository.existsByPatientIdAndCaregiverIdAndIsActiveTrue(patientId, caregiverId)) {
            throw new RuntimeException("Caregiver is already assigned to this patient");
        }
        
        // If setting as primary, unset existing primary caregiver
        if (isPrimary) {
            Optional<PatientCaregiver> existingPrimary = 
                patientCaregiverRepository.findByPatientIdAndIsPrimaryTrueAndIsActiveTrue(patientId);
            if (existingPrimary.isPresent()) {
                existingPrimary.get().setIsPrimary(false);
                patientCaregiverRepository.save(existingPrimary.get());
                logger.info("Unset previous primary caregiver for patient {}", patientId);
            }
        }
        
        // Create new assignment
        PatientCaregiver assignment = new PatientCaregiver(patientId, caregiverId, assignedBy);
        assignment.setPatient(patient);
        assignment.setCaregiver(caregiver);
        assignment.setIsPrimary(isPrimary);
        
        PatientCaregiver saved = patientCaregiverRepository.save(assignment);
        logger.info("Successfully assigned caregiver {} to patient {}", caregiverId, patientId);
        
        return saved;
    }
    
    /**
     * Remove caregiver assignment
     */
    public void removeCaregiver(UUID patientId, UUID caregiverId) {
        logger.info("Removing caregiver {} from patient {}", caregiverId, patientId);
        
        PatientCaregiver assignment = patientCaregiverRepository
                .findByPatientIdAndCaregiverIdAndIsActiveTrue(patientId, caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver assignment not found"));
        
        assignment.setIsActive(false);
        patientCaregiverRepository.save(assignment);
        
        logger.info("Successfully removed caregiver {} from patient {}", caregiverId, patientId);
    }
    
    /**
     * Set primary caregiver
     */
    public void setPrimaryCaregiver(UUID patientId, UUID caregiverId) {
        logger.info("Setting caregiver {} as primary for patient {}", caregiverId, patientId);
        
        // Unset existing primary
        Optional<PatientCaregiver> existingPrimary = 
            patientCaregiverRepository.findByPatientIdAndIsPrimaryTrueAndIsActiveTrue(patientId);
        if (existingPrimary.isPresent()) {
            existingPrimary.get().setIsPrimary(false);
            patientCaregiverRepository.save(existingPrimary.get());
        }
        
        // Set new primary
        PatientCaregiver assignment = patientCaregiverRepository
                .findByPatientIdAndCaregiverIdAndIsActiveTrue(patientId, caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver assignment not found"));
        
        assignment.setIsPrimary(true);
        patientCaregiverRepository.save(assignment);
        
        logger.info("Successfully set caregiver {} as primary for patient {}", caregiverId, patientId);
    }
    
    /**
     * Get all active caregivers for a patient
     */
    public List<User> getCaregivers(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return patientCaregiverRepository.findCaregiversByPatient(patient);
    }
    
    /**
     * Get primary caregiver for a patient
     */
    public Optional<User> getPrimaryCaregiver(UUID patientId) {
        return patientCaregiverRepository.findByPatientIdAndIsPrimaryTrueAndIsActiveTrue(patientId)
                .map(PatientCaregiver::getCaregiver);
    }
    
    // ========== FAMILY MEMBER ASSIGNMENT METHODS ==========
    
    /**
     * Assign a family member to a patient
     */
    public PatientFamilyMember assignFamilyMember(UUID patientId, UUID familyMemberId, UUID assignedById) {
        logger.info("Assigning family member {} to patient {}", familyMemberId, patientId);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        User familyMember = userRepository.findById(familyMemberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));
        
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigning user not found"));
        
        // Validate family member role
        if (familyMember.getRole() != UserRole.FAMILY) {
            throw new RuntimeException("User must have FAMILY role to be assigned as family member");
        }
        
        // Check if assignment already exists
        if (patientFamilyMemberRepository.existsByPatientIdAndFamilyMemberIdAndIsActiveTrue(patientId, familyMemberId)) {
            throw new RuntimeException("Family member is already assigned to this patient");
        }
        
        // Check maximum family members constraint (2)
        long currentCount = patientFamilyMemberRepository.countActiveFamilyMembersByPatient(patient);
        if (currentCount >= 2) {
            throw new RuntimeException("Patient already has maximum number of family members (2)");
        }
        
        // Create new assignment
        PatientFamilyMember assignment = new PatientFamilyMember(patient, familyMember, assignedBy);
        PatientFamilyMember saved = patientFamilyMemberRepository.save(assignment);
        
        logger.info("Successfully assigned family member {} to patient {}", familyMemberId, patientId);
        return saved;
    }
    
    /**
     * Remove family member assignment
     */
    public void removeFamilyMember(UUID patientId, UUID familyMemberId) {
        logger.info("Removing family member {} from patient {}", familyMemberId, patientId);
        
        PatientFamilyMember assignment = patientFamilyMemberRepository
                .findByPatientIdAndFamilyMemberIdAndIsActiveTrue(patientId, familyMemberId)
                .orElseThrow(() -> new RuntimeException("Family member assignment not found"));
        
        assignment.setIsActive(false);
        patientFamilyMemberRepository.save(assignment);
        
        logger.info("Successfully removed family member {} from patient {}", familyMemberId, patientId);
    }
    
    /**
     * Get all active family members for a patient
     */
    public List<User> getFamilyMembers(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return patientFamilyMemberRepository.findFamilyMembersByPatient(patient);
    }
    
    /**
     * Check if patient can have more family members
     */
    public boolean canAssignMoreFamilyMembers(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        long currentCount = patientFamilyMemberRepository.countActiveFamilyMembersByPatient(patient);
        return currentCount < 2;
    }
    
    // ========== QUERY METHODS ==========
    
    /**
     * Get all patients assigned to a caregiver
     */
    public List<Patient> getPatientsByCaregiver(UUID caregiverId) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));
        return patientCaregiverRepository.findPatientsByCaregiver(caregiver);
    }
    
    /**
     * Get all patients assigned to a family member
     */
    public List<Patient> getPatientsByFamilyMember(UUID familyMemberId) {
        User familyMember = userRepository.findById(familyMemberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));
        return patientFamilyMemberRepository.findPatientsByFamilyMember(familyMember);
    }
    
    /**
     * Get patients without primary caregiver
     */
    public List<Patient> getPatientsWithoutPrimaryCaregiver() {
        return patientCaregiverRepository.findPatientsWithoutPrimaryCaregiver();
    }
    
    /**
     * Get patients without any family members
     */
    public List<Patient> getPatientsWithoutFamilyMembers() {
        return patientFamilyMemberRepository.findPatientsWithoutFamilyMembers();
    }
}