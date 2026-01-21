package com.geriatriccare.repository;

import com.geriatriccare.entity.Procedure;
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
public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {
    
    Optional<Procedure> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Procedure> findByCategory(String category);
    
    Page<Procedure> findByIsActive(boolean isActive, Pageable pageable);
    
    @Query("SELECT p FROM Procedure p WHERE p.isActive = true")
    List<Procedure> findAllActive();
    
    @Query("SELECT p FROM Procedure p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Procedure> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT DISTINCT p.category FROM Procedure p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();
}
