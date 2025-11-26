package com.geriatriccare.repository;

import com.geriatriccare.entity.DrugInteraction;
import com.geriatriccare.entity.InteractionSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, UUID> {
    
    @Query("SELECT di FROM DrugInteraction di WHERE (di.medication1Name = :med1 AND di.medication2Name = :med2) OR (di.medication1Name = :med2 AND di.medication2Name = :med1)")
    Optional<DrugInteraction> findInteraction(@Param("med1") String med1, @Param("med2") String med2);
    
    List<DrugInteraction> findBySeverity(InteractionSeverity severity);
    
    @Query("SELECT di FROM DrugInteraction di WHERE di.medication1Name = :medName OR di.medication2Name = :medName")
    List<DrugInteraction> findByMedicationName(@Param("medName") String medName);
}
