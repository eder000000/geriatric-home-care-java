package com.geriatriccare.repository;

import com.geriatriccare.entity.User;
import com.geriatriccare.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);
    
    /**
     * Check if email exists (for validation)
     */
    boolean existsByEmail(String email);
    
    /**
     * Find caregivers with patient count
     */
    @Query("SELECT u FROM User u LEFT JOIN u.patients p WHERE u.role = :role AND u.isActive = true GROUP BY u ORDER BY COUNT(p) ASC")
    List<User> findCaregiversOrderByPatientCount(@Param("role") UserRole role);

    // Check if user exists by role
boolean existsByRoleAndIsActiveTrue(UserRole role);


// Find all caregivers
@Query("SELECT u FROM User u WHERE u.role = 'CAREGIVER' AND u.isActive = true")
List<User> findAllActiveCaregivers();

// Find all family members
@Query("SELECT u FROM User u WHERE u.role = 'FAMILY' AND u.isActive = true")
List<User> findAllActiveFamilyMembers();

// Find all admins
@Query("SELECT u FROM User u WHERE u.role IN ('OWNER', 'ADMIN') AND u.isActive = true")
List<User> findAllActiveAdmins();
}