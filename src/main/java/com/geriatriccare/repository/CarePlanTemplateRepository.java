package com.geriatriccare.repository;

import com.geriatriccare.entity.CarePlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarePlanTemplateRepository extends JpaRepository<CarePlanTemplate, UUID> {
    
    Optional<CarePlanTemplate> findByIdAndIsActiveTrue(UUID id);
    
    List<CarePlanTemplate> findByIsActiveTrue();
    
    List<CarePlanTemplate> findByCategoryAndIsActiveTrue(String category);
    
    List<CarePlanTemplate> findByCreatedByIdAndIsActiveTrue(UUID createdById);
}