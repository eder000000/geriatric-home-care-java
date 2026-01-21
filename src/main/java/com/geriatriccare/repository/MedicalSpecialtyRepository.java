package com.geriatriccare.repository;

import com.geriatriccare.entity.MedicalSpecialty;
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
public interface MedicalSpecialtyRepository extends JpaRepository<MedicalSpecialty, UUID> {
    
    Optional<MedicalSpecialty> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<MedicalSpecialty> findByIsActive(boolean isActive, Pageable pageable);
    
    @Query("SELECT s FROM MedicalSpecialty s WHERE s.isActive = true")
    List<MedicalSpecialty> findAllActive();
    
    List<MedicalSpecialty> findByParentSpecialty(MedicalSpecialty parentSpecialty);
    
    @Query("SELECT s FROM MedicalSpecialty s WHERE s.parentSpecialty IS NULL AND s.isActive = true")
    List<MedicalSpecialty> findTopLevelSpecialties();
    
    @Query("SELECT s FROM MedicalSpecialty s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<MedicalSpecialty> search(@Param("query") String query, Pageable pageable);
}
