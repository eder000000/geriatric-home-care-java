package com.geriatriccare.repository;

import com.geriatriccare.entity.CareTask;
import com.geriatriccare.entity.CareTaskCategory;
import com.geriatriccare.entity.CareTaskPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CareTaskRepository extends JpaRepository<CareTask, UUID> {
    
    // Find by basic properties
    Optional<CareTask> findByIdAndIsActiveTrue(UUID id);
    Page<CareTask> findByIsActiveTrue(Pageable pageable);
    
    // Find by care plan
    List<CareTask> findByCarePlanIdAndIsActiveTrue(UUID carePlanId);
    
    // Find by category
    List<CareTask> findByCategoryAndIsActiveTrue(CareTaskCategory category);
    
    // Find by priority
    List<CareTask> findByPriorityAndIsActiveTrue(CareTaskPriority priority);
    
    // Find by frequency
    List<CareTask> findByFrequencyAndIsActiveTrue(com.geriatriccare.entity.CareTaskFrequency frequency);
    
    // Find tasks requiring caregiver presence
    List<CareTask> findByRequiresCaregiverPresenceTrueAndIsActiveTrue();
    
    // Count tasks by care plan
    @Query("SELECT COUNT(ct) FROM CareTask ct WHERE ct.carePlan.id = :carePlanId AND ct.isActive = true")
    long countTasksByCarePlan(@Param("carePlanId") UUID carePlanId);
    
    // Find tasks by care plan and category
    List<CareTask> findByCarePlanIdAndCategoryAndIsActiveTrue(UUID carePlanId, CareTaskCategory category);
    
    // Find tasks by care plan and priority
    List<CareTask> findByCarePlanIdAndPriorityAndIsActiveTrue(UUID carePlanId, CareTaskPriority priority);
}