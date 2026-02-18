package com.geriatriccare.controller;

import com.geriatriccare.dto.vitalsign.VitalSignRequest;
import com.geriatriccare.dto.vitalsign.VitalSignResponse;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.service.VitalSignService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vital-signs")
public class VitalSignController {

    private static final Logger logger = LoggerFactory.getLogger(VitalSignController.class);
    private final VitalSignService vitalSignService;

    public VitalSignController(VitalSignService vitalSignService) {
        this.vitalSignService = vitalSignService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER')")
    public ResponseEntity<VitalSignResponse> recordVitalSign(
            @Valid @RequestBody VitalSignRequest request) {
        logger.info("Recording vital sign for patient: {}", request.getPatientId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vitalSignService.recordVitalSign(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<VitalSignResponse> getVitalSign(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(vitalSignService.getVitalSignById(id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Vital sign not found with id: " + id);
        }
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<List<VitalSignResponse>> getPatientVitalSigns(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(vitalSignService.getVitalSignsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<VitalSignResponse> getLatestVitalSign(@PathVariable UUID patientId) {
        try {
            return ResponseEntity.ok(vitalSignService.getLatestVitalSign(patientId));
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("No vital signs found for patient: " + patientId);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN')")
    public ResponseEntity<Void> deleteVitalSign(@PathVariable UUID id) {
        try {
            vitalSignService.deleteVitalSign(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Vital sign not found with id: " + id);
        }
    }
}
