package com.geriatriccare.repository;

import com.geriatriccare.entity.PromptTemplate;
import com.geriatriccare.entity.PromptCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {
    
    /**
     * Find all active templates ordered by category and name
     */
    List<PromptTemplate> findByIsActiveTrueOrderByCategoryAscNameAsc();
    
    /**
     * Find all versions of a template by name, ordered by version descending
     */
    List<PromptTemplate> findByNameOrderByVersionDesc(String name);
    
    /**
     * Get the highest version number for a template name
     */
    @Query("SELECT MAX(pt.version) FROM PromptTemplate pt WHERE pt.name = :name")
    Integer findMaxVersionByName(@Param("name") String name);
    
    /**
     * Check if a template name already exists
     */
    boolean existsByName(String name);
    
    /**
     * Find templates by category, ordered by name and version
     */
    List<PromptTemplate> findByCategoryOrderByNameAscVersionDesc(PromptCategory category);
}