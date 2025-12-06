package com.geriatriccare.repository;

import com.geriatriccare.entity.PromptCategory;
import com.geriatriccare.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {
    
    // Find active templates by category
    List<PromptTemplate> findByCategoryAndIsActiveTrue(PromptCategory category);
    
    // Find by name and version
    Optional<PromptTemplate> findByNameAndVersion(String name, Integer version);
    
    // Find active template by name (latest version)
    @Query("SELECT pt FROM PromptTemplate pt WHERE pt.name = :name AND pt.isActive = true ORDER BY pt.version DESC")
    Optional<PromptTemplate> findLatestActiveByName(@Param("name") String name);
    
    // Find all active templates
    List<PromptTemplate> findByIsActiveTrueOrderByCategoryAscNameAsc();
    
    // Find all versions of a template
    List<PromptTemplate> findByNameOrderByVersionDesc(String name);
    
    // Get latest version number for a template name
    @Query("SELECT MAX(pt.version) FROM PromptTemplate pt WHERE pt.name = :name")
    Integer findMaxVersionByName(@Param("name") String name);
    
    // Check if template name exists
    boolean existsByName(String name);
    
    // Find templates by category (all versions)
    List<PromptTemplate> findByCategoryOrderByNameAscVersionDesc(PromptCategory category);
}