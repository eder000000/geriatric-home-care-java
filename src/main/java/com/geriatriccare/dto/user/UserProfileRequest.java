package com.geriatriccare.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Profile Request DTO
 * For user profile updates (non-admin fields)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String profilePictureUrl;

    /**
     * User preferences as JSON string
     * Example: {"theme":"dark","notifications":true,"language":"en"}
     */
    private String preferences;
}
