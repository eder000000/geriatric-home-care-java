package com.geriatriccare.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User Statistics Response DTO
 * System-wide user statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsResponse {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long suspendedUsers;
    private long pendingVerification;
    private long usersWithMfaEnabled;
    private long lockedUsers;
    private long usersWithExpiredPasswords;
    
    // Users by role
    private long adminCount;
    private long physicianCount;
    private long caregiverCount;
    private long familyCount;
    private long patientCount;
    
    private Map<String, Long> usersByRole;
    private Map<String, Long> usersByStatus;
    
    private LocalDateTime generatedAt;
}
