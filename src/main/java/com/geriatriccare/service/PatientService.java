package com.geriatriccare.service;

import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import com.geriatriccare.dto.summary.CaregiverSummary;
import com.geriatriccare.dto.summary.FamilyMemberSummary;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.PatientCaregiver;
import com.geriatriccare.entity.PatientFamilyMember;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.PatientCaregiverRepository;
import com.geriatriccare.repository.PatientFamilyMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private PatientCaregiverRepository patientCaregiverRepository;
    
    @Autowired
    private PatientFamilyMemberRepository patientFamilyMemberRepository;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new patient
     */
    public PatientResponse createPatient(PatientRequest request) {
        logger.info("Creating new patient: {} {}", request.getFirstName(), request.getLastName());
        
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setMedicalConditions(request.getMedicalConditions());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setEmergencyPhone(request.getEmergencyPhone());
        
        Patient saved = patientRepository.save(patient);
        logger.info("Successfully created patient with ID: {}", saved.getId());
        
        return convertToResponse(saved);
    }
    
    /**
     * Get patient by ID
     */
    @Transactional(readOnly = true)
    public Optional<PatientResponse> getPatientById(UUID id) {
        logger.debug("Fetching patient by ID: {}", id);
        
        return patientRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToResponse);
    }
    
    /**
     * Get all active patients with pagination
     */
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching patients - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                    page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Fix: Use findAll with pageable instead of findByIsActiveTrue
        return patientRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Update patient information
     */
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        logger.info("Updating patient: {}", id);
        
        Patient patient = patientRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Update fields
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setMedicalConditions(request.getMedicalConditions());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setEmergencyPhone(request.getEmergencyPhone());
        
        Patient updated = patientRepository.save(patient);
        logger.info("Successfully updated patient: {}", id);
        
        return convertToResponse(updated);
    }
    
    /**
     * Soft delete patient
     */
    public void deletePatient(UUID id) {
        logger.info("Soft deleting patient: {}", id);
        
        Patient patient = patientRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patient.setIsActive(false);
        patientRepository.save(patient);
        
        logger.info("Successfully soft deleted patient: {}", id);
    }
    
    // ========== SEARCH OPERATIONS ==========
    
    /**
     * Search patients by name
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatientsByName(String name) {
        logger.debug("Searching patients by name: {}", name);
        
        return patientRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Search patients by age range
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatientsByAgeRange(int minAge, int maxAge) {
        logger.debug("Searching patients by age range: {} - {}", minAge, maxAge);
        
        return patientRepository.findByAgeBetween(minAge, maxAge)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Search patients by medical condition
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatientsByCondition(String condition) {
        logger.debug("Searching patients by medical condition: {}", condition);
        
        return patientRepository.findByMedicalConditionsContaining(condition)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Check if patient exists and is active
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientRepository.findByIdAndIsActiveTrue(id).isPresent();
    }
    
    /**
     * Get total count of active patients
     */
    @Transactional(readOnly = true)
    public long getTotalActivePatients() {
        return patientRepository.countActivePatients();
    }
    
    // ========== CONVERSION METHODS ==========
    
    /**
     * Convert Patient entity to PatientResponse DTO
     */
    private PatientResponse convertToResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        
        // Basic patient information
        response.setId(patient.getId());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setMedicalConditions(patient.getMedicalConditions());
        response.setEmergencyContact(patient.getEmergencyContact());
        response.setEmergencyPhone(patient.getEmergencyPhone());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        response.setIsActive(patient.getIsActive());
        response.setAge(patient.getAge());
        response.setFullName(patient.getFullName());
        
        // Load and convert caregivers
        List<PatientCaregiver> caregiverAssignments = patientCaregiverRepository
                .findByPatientIdAndIsActiveTrue(patient.getId());
        
        List<CaregiverSummary> caregivers = caregiverAssignments.stream()
                .map(pc -> new CaregiverSummary(
                        pc.getCaregiver().getId(),
                        pc.getCaregiver().getFirstName(),
                        pc.getCaregiver().getLastName(),
                        pc.getCaregiver().getEmail(),
                        pc.getIsPrimary()
                ))
                .collect(Collectors.toList());
        response.setCaregivers(caregivers);
        
        // Set primary caregiver
        caregivers.stream()
                .filter(CaregiverSummary::isPrimary)
                .findFirst()
                .ifPresent(response::setPrimaryCaregiver);
        
        // Load and convert family members
        List<PatientFamilyMember> familyAssignments = patientFamilyMemberRepository
                .findByPatientIdAndIsActiveTrue(patient.getId());
        
        List<FamilyMemberSummary> familyMembers = familyAssignments.stream()
                .map(pfm -> new FamilyMemberSummary(
                        pfm.getFamilyMember().getId(),
                        pfm.getFamilyMember().getFirstName(),
                        pfm.getFamilyMember().getLastName(),
                        pfm.getFamilyMember().getEmail()
                ))
                .collect(Collectors.toList());
        response.setFamilyMembers(familyMembers);
        
        return response;
    }
}