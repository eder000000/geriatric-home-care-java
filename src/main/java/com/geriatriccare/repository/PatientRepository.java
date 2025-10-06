package com.geriatriccare.repository;

import com.geriatriccare.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByIdAndIsActiveTrue(UUID id);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrue(
        String firstName, String lastName);

    List<Patient> findByDateOfBirthBetweenAndIsActiveTrue(LocalDate startDate, LocalDate endDate);

    Page<Patient> findByIsActiveTrue(Pageable pageable);

    long countByIsActiveTrue();

    List<Patient> findByMedicalConditionsContainingIgnoreCaseAndIsActiveTrue(String condition);
    
    // Add these missing methods that PatientService calls:
    //List<Patient> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge AND p.isActive = true")
    List<Patient> findByAgeBetween(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    List<Patient> findByMedicalConditionsContaining(String condition);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isActive = true")
    long countActivePatients();
}