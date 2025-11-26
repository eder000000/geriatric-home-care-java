package com.geriatriccare.service;

import com.geriatriccare.dto.DosageCalculationRequest;
import com.geriatriccare.dto.DosageCalculationResponse;
import com.geriatriccare.dto.InteractionCheckRequest;
import com.geriatriccare.dto.InteractionCheckResponse;
import com.geriatriccare.dto.MedicationRequest;
import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.DrugInteraction;
import com.geriatriccare.entity.Medication;
import com.geriatriccare.entity.MedicationForm;
import com.geriatriccare.repository.DrugInteractionRepository;
import com.geriatriccare.repository.MedicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final DrugInteractionRepository drugInteractionRepository;

    public MedicationService(MedicationRepository medicationRepository,
                           DrugInteractionRepository drugInteractionRepository) {
        this.medicationRepository = medicationRepository;
        this.drugInteractionRepository = drugInteractionRepository;
    }

    public MedicationResponse createMedication(MedicationRequest request) {
        validateRequest(request);
        Medication medication = new Medication();
        mapRequestToEntity(request, medication);
        medication.setActive(true);
        Medication saved = medicationRepository.save(medication);
        return mapEntityToResponse(saved);
    }

    public MedicationResponse getMedicationById(UUID id) {
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        return mapEntityToResponse(medication);
    }

    public List<MedicationResponse> getAllMedications() {
        return medicationRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public MedicationResponse updateMedication(UUID id, MedicationRequest request) {
        validateRequest(request);
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        mapRequestToEntity(request, medication);
        Medication updated = medicationRepository.save(medication);
        return mapEntityToResponse(updated);
    }

    public void deleteMedication(UUID id) {
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        medication.setActive(false);
        medicationRepository.save(medication);
    }

    public List<MedicationResponse> searchByName(String name) {
        return medicationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name)
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> findByForm(MedicationForm form) {
        return medicationRepository.findByFormAndIsActiveTrue(form)
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> findExpiringSoon(int days) {
        LocalDate thresholdDate = LocalDate.now().plusDays(days);
        return medicationRepository.findByExpirationDateBeforeAndIsActiveTrue(thresholdDate)
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> findLowStock() {
        return medicationRepository.findLowStockMedications()
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public MedicationResponse adjustStock(UUID id, int quantityChange) {
        if (quantityChange == 0) {
            throw new RuntimeException("Quantity change cannot be zero");
        }
        
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        
        int newQuantity = medication.getQuantityInStock() + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock. Current: " + medication.getQuantityInStock() + 
                                     ", Requested change: " + quantityChange);
        }
        
        medication.setQuantityInStock(newQuantity);
        Medication updated = medicationRepository.save(medication);
        return mapEntityToResponse(updated);
    }

    public boolean needsReorder(UUID id) {
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        return medication.getQuantityInStock() <= medication.getReorderLevel();
    }

    public int calculateReorderQuantity(UUID id) {
        Medication medication = medicationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        
        if (!needsReorder(id)) {
            return 0;
        }
        
        int optimalStock = medication.getReorderLevel() * 2;
        return optimalStock - medication.getQuantityInStock();
    }

    private MedicationResponse mapEntityToResponse(Medication medication) {
        MedicationResponse response = new MedicationResponse();
        response.setId(medication.getId());
        response.setName(medication.getName());
        response.setGenericName(medication.getGenericName());
        response.setDosage(medication.getDosage());
        response.setForm(medication.getForm());
        response.setManufacturer(medication.getManufacturer());
        response.setExpirationDate(medication.getExpirationDate());
        response.setQuantityInStock(medication.getQuantityInStock());
        response.setReorderLevel(medication.getReorderLevel());
        response.setActive(medication.isActive());
        response.setCreatedAt(medication.getCreatedAt());
        response.setUpdatedAt(medication.getUpdatedAt());
        return response;
    }

    private void validateRequest(MedicationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Medication name is required");
        }
        if (request.getDosage() == null || request.getDosage().trim().isEmpty()) {
            throw new RuntimeException("Dosage is required");
        }
        if (request.getForm() == null) {
            throw new RuntimeException("Medication form is required");
        }
        if (request.getExpirationDate() != null && request.getExpirationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Expiration date must be in the future");
        }
        if (request.getQuantityInStock() != null && request.getQuantityInStock() < 0) {
            throw new RuntimeException("Quantity cannot be negative");
        }
        if (request.getReorderLevel() != null && request.getReorderLevel() < 0) {
            throw new RuntimeException("Reorder level cannot be negative");
        }
    }

    private void mapRequestToEntity(MedicationRequest request, Medication medication) {
        medication.setName(request.getName());
        medication.setGenericName(request.getGenericName());
        medication.setDosage(request.getDosage());
        medication.setForm(request.getForm());
        medication.setManufacturer(request.getManufacturer());
        medication.setExpirationDate(request.getExpirationDate());
        medication.setQuantityInStock(request.getQuantityInStock());
        medication.setReorderLevel(request.getReorderLevel());
    }

    // ========================================
    // DOSAGE CALCULATIONS
    // ========================================

    public DosageCalculationResponse calculateWeightBasedDosage(DosageCalculationRequest request) {
        if (request.getPatientWeight() == null || request.getPatientWeight() <= 0) {
            throw new RuntimeException("Weight must be positive");
        }
        medicationRepository.findByIdAndIsActiveTrue(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        double dosage = request.getPatientWeight() * request.getDosagePerKg();
        DosageCalculationResponse response = new DosageCalculationResponse();
        response.setCalculatedDosage(dosage);
        return response;
    }

    public DosageCalculationResponse calculateAgeBasedDosage(DosageCalculationRequest request) {
        if (request.getPatientAge() == null || request.getPatientAge() < 0) {
            throw new RuntimeException("Age must be positive");
        }
        medicationRepository.findByIdAndIsActiveTrue(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        double dosage = request.getBaseDosage();
        String ageGroup;
        
        if (request.getPatientAge() < 18) {
            dosage *= 0.5;
            ageGroup = "PEDIATRIC";
        } else if (request.getPatientAge() >= 65) {
            dosage *= 0.75;
            ageGroup = "GERIATRIC";
        } else {
            ageGroup = "ADULT";
        }
        
        DosageCalculationResponse response = new DosageCalculationResponse();
        response.setCalculatedDosage(dosage);
        response.setAgeGroup(ageGroup);
        return response;
    }

    public boolean validateMaximumDailyDose(DosageCalculationRequest request) {
        double totalDailyDose = request.getCalculatedDosage() * request.getFrequency();
        return totalDailyDose <= request.getMaxDailyDose();
    }

    public double calculateTotalDailyDose(DosageCalculationRequest request) {
        return request.getCalculatedDosage() * request.getFrequency();
    }

    public DosageCalculationResponse adjustDosageForRenalFunction(DosageCalculationRequest request) {
        if (request.getGlomerularFiltrationRate() < 0) {
            throw new RuntimeException("GFR must be positive");
        }
        medicationRepository.findByIdAndIsActiveTrue(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        double dosage = request.getBaseDosage();
        double gfr = request.getGlomerularFiltrationRate();
        
        if (gfr < 15) {
            dosage *= 0.25;
        } else if (gfr < 30) {
            dosage *= 0.5;
        } else if (gfr < 60) {
            dosage *= 0.75;
        }
        
        DosageCalculationResponse response = new DosageCalculationResponse();
        response.setCalculatedDosage(dosage);
        response.setRenalAdjustmentApplied(true);
        return response;
    }

    public DosageCalculationResponse adjustDosageForHepaticFunction(DosageCalculationRequest request) {
        if (!request.getChildPughScore().matches("[ABC]")) {
            throw new RuntimeException("Invalid Child-Pugh score. Must be A, B, or C");
        }
        medicationRepository.findByIdAndIsActiveTrue(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        double dosage = request.getBaseDosage();
        
        switch (request.getChildPughScore()) {
            case "B":
                dosage *= 0.75;
                break;
            case "C":
                dosage *= 0.5;
                break;
        }
        
        DosageCalculationResponse response = new DosageCalculationResponse();
        response.setCalculatedDosage(dosage);
        response.setHepaticAdjustmentApplied(true);
        return response;
    }

    public DosageCalculationResponse calculateComplexDosage(DosageCalculationRequest request) {
        medicationRepository.findByIdAndIsActiveTrue(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        double dosage = request.getPatientWeight() * request.getDosagePerKg();
        DosageCalculationResponse response = new DosageCalculationResponse();
        
        if (request.getPatientAge() < 18) {
            dosage *= 0.5;
            response.setAgeGroup("PEDIATRIC");
            response.addWarning("Pediatric patient - reduced dosage applied");
        } else if (request.getPatientAge() >= 65) {
            dosage *= 0.75;
            response.setAgeGroup("GERIATRIC");
            response.addWarning("Geriatric patient - monitor closely");
        } else {
            response.setAgeGroup("ADULT");
        }
        
        if (request.getGlomerularFiltrationRate() != null) {
            double gfr = request.getGlomerularFiltrationRate();
            if (gfr < 15) {
                dosage *= 0.25;
                response.addWarning("End-stage renal disease - dosage significantly reduced");
            } else if (gfr < 30) {
                dosage *= 0.5;
                response.addWarning("Severe renal impairment - dosage reduced");
            } else if (gfr < 60) {
                dosage *= 0.75;
                response.addWarning("Moderate renal impairment - dosage adjusted");
            }
            response.setRenalAdjustmentApplied(true);
        }
        
        if (request.getChildPughScore() != null) {
            switch (request.getChildPughScore()) {
                case "B":
                    dosage *= 0.75;
                    response.addWarning("Moderate hepatic impairment - dosage adjusted");
                    break;
                case "C":
                    dosage *= 0.5;
                    response.addWarning("Severe hepatic impairment - dosage significantly reduced");
                    break;
            }
            response.setHepaticAdjustmentApplied(true);
        }
        
        response.setCalculatedDosage(dosage);
        return response;
    }

    // ========================================
    // DRUG INTERACTION CHECKING
    // ========================================

    public InteractionCheckResponse checkDrugInteractions(InteractionCheckRequest request) {
        if (request.getMedicationNames() == null) {
            throw new RuntimeException("Medication names cannot be null");
        }
        
        InteractionCheckResponse response = new InteractionCheckResponse();
        List<String> medicationNames = request.getMedicationNames();
        
        if (medicationNames.size() < 2) {
            return response;
        }
        
        for (int i = 0; i < medicationNames.size(); i++) {
            for (int j = i + 1; j < medicationNames.size(); j++) {
                String med1 = medicationNames.get(i);
                String med2 = medicationNames.get(j);
                
                if (med1.equals(med2)) {
                    continue;
                }
                
                Optional<DrugInteraction> interaction = 
                    drugInteractionRepository.findInteraction(med1, med2);
                
                if (interaction.isPresent()) {
                    DrugInteraction di = interaction.get();
                    response.addInteraction(
                        di.getMedication1Name(),
                        di.getMedication2Name(),
                        di.getSeverity(),
                        di.getDescription(),
                        di.getRecommendation()
                    );
                }
            }
        }
        
        return response;
    }

    public InteractionCheckResponse checkDrugInteractionsByIds(InteractionCheckRequest request) {
        List<String> medicationNames = request.getMedicationIds().stream()
                .map(id -> medicationRepository.findByIdAndIsActiveTrue(id)
                        .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id)))
                .map(Medication::getName)
                .collect(Collectors.toList());
        
        InteractionCheckRequest nameRequest = new InteractionCheckRequest();
        nameRequest.setMedicationNames(medicationNames);
        
        return checkDrugInteractions(nameRequest);
    }

    // Additional search and query methods
    public List<MedicationResponse> findByManufacturer(String manufacturer) {
        return medicationRepository.findByManufacturerIgnoreCaseAndIsActiveTrue(manufacturer)
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicationResponse> findExpired() {
        LocalDate today = LocalDate.now();
        return medicationRepository.findExpiredMedications()
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public int countAllMedications() {
        return medicationRepository.findByIsActiveTrue().size();
    }

    public int calculateTotalStock() {
        return medicationRepository.findByIsActiveTrue()
                .stream()
                .mapToInt(Medication::getQuantityInStock)
                .sum();
    }

    public int countNeedingReorder() {
        return medicationRepository.findLowStockMedications().size();
    }

    public int countExpired() {
        return medicationRepository.findExpiredMedications().size();
    }
}
