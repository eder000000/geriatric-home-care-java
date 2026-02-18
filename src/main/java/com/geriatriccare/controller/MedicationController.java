package com.geriatriccare.controller;

import com.geriatriccare.dto.MedicationRequest;
import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.User;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.UserPrincipal;
import com.geriatriccare.service.MedicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private static final Logger logger = LoggerFactory.getLogger(MedicationController.class);

    private final MedicationService medicationService;
    private final UserRepository userRepository;

    public MedicationController(MedicationService medicationService, UserRepository userRepository) {
        this.medicationService = medicationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<MedicationResponse> createMedication(
            @Valid @RequestBody MedicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("Creating medication: {}", request.getName());
        User currentUser = resolveUser(principal);
        MedicationResponse response = medicationService.createMedication(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<MedicationResponse> getMedication(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(medicationService.getMedicationById(id));
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Medication not found with id: " + id);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<MedicationResponse>> getAllMedications() {
        return ResponseEntity.ok(medicationService.getAllMedications());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN')")
    public ResponseEntity<MedicationResponse> updateMedication(
            @PathVariable UUID id,
            @Valid @RequestBody MedicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            User currentUser = resolveUser(principal);
            return ResponseEntity.ok(medicationService.updateMedication(id, request, currentUser));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Medication not found with id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedication(@PathVariable UUID id) {
        try {
            medicationService.deleteMedication(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Medication not found with id: " + id);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<MedicationResponse>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(medicationService.searchByName(name));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<MedicationResponse>> getLowStock() {
        return ResponseEntity.ok(medicationService.findLowStock());
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<List<MedicationResponse>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(medicationService.findExpiringSoon(days));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<MedicationResponse> adjustStock(
            @PathVariable UUID id,
            @RequestParam int quantityChange) {
        try {
            return ResponseEntity.ok(medicationService.adjustStock(id, quantityChange));
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Medication not found with id: " + id);
        }
    }

    private User resolveUser(UserPrincipal principal) {
        return userRepository.findByEmailAndIsActiveTrue(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
