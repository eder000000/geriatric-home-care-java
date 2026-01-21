package com.geriatriccare.repository;

import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository
 * Data access layer for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (for login)
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username (for login)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email or username
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find users by role (paginated)
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by status (paginated)
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Find users by role and status
     */
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    /**
     * Find active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.deleted = false")
    List<User> findActiveUsers();

    /**
     * Find active users (paginated)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.deleted = false")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * Search users by name, email, or username
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    /**
     * Find users by email verification status
     */
    List<User> findByEmailVerified(boolean verified);

    /**
     * Find users with MFA enabled
     */
    List<User> findByMfaEnabled(boolean enabled);

    /**
     * Find users with expired passwords
     */
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :expirationDate AND u.deleted = false")
    List<User> findUsersWithExpiredPasswords(@Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Find locked users
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil > :now AND u.deleted = false")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Find users that must change password
     */
    List<User> findByMustChangePassword(boolean mustChange);

    /**
     * Find by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Count active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE' AND u.deleted = false")
    long countActiveUsers();

    /**
     * Count pending verification users
     */
    long countByStatus(UserStatus status);

    /**
     * Find users created by specific admin
     */
    List<User> findByCreatedBy(String createdBy);

    /**
     * Find recently created users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyCreated(@Param("since") LocalDateTime since);
}
