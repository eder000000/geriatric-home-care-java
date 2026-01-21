package com.geriatriccare.repository;

import com.geriatriccare.entity.Diagnosis;
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
public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {
    
    Optional<Diagnosis> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Diagnosis> findByCategory(String category);
    
    Page<Diagnosis> findByIsActive(boolean isActive, Pageable pageable);
    
    @Query("SELECT d FROM Diagnosis d WHERE d.isActive = true")
    List<Diagnosis> findAllActive();
    
    @Query("SELECT d FROM Diagnosis d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Diagnosis> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT DISTINCT d.category FROM Diagnosis d WHERE d.category IS NOT NULL ORDER BY d.category")
    List<String> findAllCategories();
}
