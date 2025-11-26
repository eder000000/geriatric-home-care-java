package com.geriatriccare.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    Optional<Medication> findByIdAndIsActiveTrue(UUID id);

    List<Medication> findByIsActiveTrue();

    List<Medication> findByNameContainingIgnoreCaseAndIsActiveTrue (String name);

    List<Medication> findByFormAndIsActiveTrue(MedicationForm tablet);

    List<Medication> findByManufacturerIgnoreCaseAndIsActiveTrue (String manufacturer);

    @Query("SELECT m FROM Medication m WHERE m.isActive = true AND m.quantityInStock <= m.reorderLevel")
    List<Medication> findLowStockMedications();

    @Query("SELECT m FROM Medication m WHERE m.isActive = true AND m.expirationDate < CURRENT_DATE")
    List<Medication> findExpiredMedications();   

    @Query("SELECT m FROM Medication m WHERE m.isActive = true AND m.expirationDate BETWEEN CURRENT_DATE AND :futureDate")
    List<Medication> findExpiringSoon(@Param("futureDate") LocalDate futureDate);
    List<Medication> findByExpirationDateBeforeAndIsActiveTrue(LocalDate date);
}
