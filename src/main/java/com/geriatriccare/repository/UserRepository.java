package com.geriatriccare.repository;

import com.geriatriccare.entity.User;
import com.geriatriccare.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Find by email
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    boolean existsByEmailAndIsActiveTrue(String email);
    
    // Find by ID and active status
    Optional<User> findByIdAndIsActiveTrue(UUID id);
    
    // Find by role
    List<User> findByRoleAndIsActiveTrue(UserRole role);
    boolean existsByRoleAndIsActiveTrue(UserRole role);
    
    // Find all active users
    List<User> findByIsActiveTrue();
    
    // Find all caregivers
    @Query("SELECT u FROM User u WHERE u.role = 'CAREGIVER' AND u.isActive = true")
    List<User> findAllActiveCaregivers();
    
    // Find all family members
    @Query("SELECT u FROM User u WHERE u.role = 'FAMILY' AND u.isActive = true")
    List<User> findAllActiveFamilyMembers();
    
    // Find all admins (including owners)
    @Query("SELECT u FROM User u WHERE u.role IN ('OWNER', 'ADMIN') AND u.isActive = true")
    List<User> findAllActiveAdmins();
    
    // Find users by first name or last name (case insensitive)
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Find users by role and name
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<User> findByRoleAndNameContainingIgnoreCase(UserRole role, String name);
}