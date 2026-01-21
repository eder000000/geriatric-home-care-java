package com.geriatriccare.repository;

import com.geriatriccare.entity.DrugCatalog;
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
public interface DrugCatalogRepository extends JpaRepository<DrugCatalog, UUID> {
    
    Optional<DrugCatalog> findByGenericName(String genericName);
    
    boolean existsByGenericName(String genericName);
    
    List<DrugCatalog> findByCategory(String category);
    
    Page<DrugCatalog> findByIsActive(boolean isActive, Pageable pageable);
    
    @Query("SELECT d FROM DrugCatalog d WHERE d.isActive = true")
    List<DrugCatalog> findAllActive();
    
    @Query("SELECT d FROM DrugCatalog d WHERE " +
           "LOWER(d.genericName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.brandNames) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<DrugCatalog> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT DISTINCT d.category FROM DrugCatalog d WHERE d.category IS NOT NULL ORDER BY d.category")
    List<String> findAllCategories();
    
    List<DrugCatalog> findByTherapeuticClass(String therapeuticClass);
}
