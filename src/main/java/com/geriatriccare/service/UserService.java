package com.geriatriccare.service;

import com.geriatriccare.dto.user.ChangePasswordRequest;
import com.geriatriccare.dto.user.UserRequest;
import com.geriatriccare.dto.user.UserResponse;
import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.model.User;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.service.security.PasswordPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PasswordPolicyService passwordPolicyService;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        passwordPolicyService.validatePassword(request.getPassword(), request.getEmail());

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setIsActive(true);
        user.setPasswordChangedAt(LocalDateTime.now());
        user = userRepository.save(user);

        passwordPolicyService.addToPasswordHistory(user.getId(), user.getPassword());
        return convertToResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password incorrect");
        }

        passwordPolicyService.validatePassword(request.getNewPassword(), user.getEmail());

        if (passwordPolicyService.isPasswordInHistory(userId, request.getNewPassword())) {
            throw new InvalidPasswordException("Cannot reuse last 5 passwords");
        }

        String encoded = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encoded);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordPolicyService.addToPasswordHistory(userId, encoded);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return convertToResponse(userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByIsActiveTrue().stream()
            .map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setFirstName(user.getFirstName());
        r.setLastName(user.getLastName());
        r.setEmail(user.getEmail());
        r.setPhone(user.getPhone());
        r.setRole(user.getRole().name());
        r.setIsActive(user.getIsActive());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
