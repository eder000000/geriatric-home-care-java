package com.geriatriccare.service;

import com.geriatriccare.dto.vitalsign.VitalSignRequest;
import com.geriatriccare.dto.vitalsign.VitalSignResponse;
import com.geriatriccare.dto.vitalsign.VitalSignStatistics;
import com.geriatriccare.entity.VitalSign;
import com.geriatriccare.enums.TrendDirection;
import com.geriatriccare.enums.VitalSignType;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.VitalSignRepository;
import com.geriatriccare.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VitalSignService {

    private final VitalSignRepository vitalSignRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public VitalSignResponse recordVitalSign(VitalSignRequest request) {
        log.info("Recording vital sign for patient: {}", request.getPatientId());

        VitalSign vitalSign = new VitalSign();
        vitalSign.setPatientId(request.getPatientId());
        vitalSign.setMeasuredAt(request.getMeasuredAt() != null ? request.getMeasuredAt() : LocalDateTime.now());
        vitalSign.setBloodPressureSystolic(request.getBloodPressureSystolic());
        vitalSign.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        vitalSign.setHeartRate(request.getHeartRate());
        vitalSign.setTemperature(request.getTemperature());
        vitalSign.setRespiratoryRate(request.getRespiratoryRate());
        vitalSign.setOxygenSaturation(request.getOxygenSaturation());
        vitalSign.setPosition(request.getPosition());
        vitalSign.setMeasurementMethod(request.getMeasurementMethod());
        vitalSign.setNotes(request.getNotes());
        vitalSign.setRecordedBy(securityUtil.getCurrentUserId());
        vitalSign.setDeleted(false);

        vitalSign = vitalSignRepository.save(vitalSign);
        log.info("Vital sign recorded successfully: {}", vitalSign.getId());

        return convertToResponse(vitalSign);
    }

    @Transactional(readOnly = true)
    public VitalSignResponse getVitalSignById(UUID id) {
        VitalSign vitalSign = vitalSignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vital sign not found"));
        return convertToResponse(vitalSign);
    }

    @Transactional(readOnly = true)
    public List<VitalSignResponse> getVitalSignsByPatient(UUID patientId) {
        return vitalSignRepository.findByPatientIdAndDeletedFalseOrderByMeasuredAtDesc(patientId)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VitalSignResponse> getVitalSignsByPatientPaginated(UUID patientId, Pageable pageable) {
        return vitalSignRepository.findByPatientIdAndDeletedFalse(patientId, pageable)
            .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<VitalSignResponse> getVitalSignsByDateRange(UUID patientId, LocalDateTime start, LocalDateTime end) {
        return vitalSignRepository.findByPatientIdAndDateRange(patientId, start, end)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VitalSignResponse getLatestVitalSign(UUID patientId) {
        return vitalSignRepository.findLatestByPatientId(patientId)
            .map(this::convertToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("No vital signs found for patient"));
    }

    @Transactional
    public void deleteVitalSign(UUID id) {
        VitalSign vitalSign = vitalSignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vital sign not found"));
        vitalSign.setDeleted(true);
        vitalSignRepository.save(vitalSign);
        log.info("Vital sign deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public VitalSignStatistics calculateStatistics(UUID patientId, VitalSignType type, int days) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        LocalDateTime end = LocalDateTime.now();
        
        List<VitalSign> vitalSigns = vitalSignRepository.findByPatientIdAndDateRange(patientId, start, end);
        
        if (vitalSigns.isEmpty()) {
            throw new ResourceNotFoundException("No vital signs found for the specified period");
        }

        List<Double> values = extractValues(vitalSigns, type);
        
        if (values.isEmpty()) {
            throw new ResourceNotFoundException("No data available for " + type);
        }

        return VitalSignStatistics.builder()
            .type(type)
            .mean(calculateMean(values))
            .median(calculateMedian(values))
            .standardDeviation(calculateStandardDeviation(values))
            .min(Collections.min(values))
            .max(Collections.max(values))
            .trend(calculateTrend(values))
            .dataPoints(values.size())
            .build();
    }

    private List<Double> extractValues(List<VitalSign> vitalSigns, VitalSignType type) {
        List<Double> values = new ArrayList<>();
        
        for (VitalSign vs : vitalSigns) {
            switch (type) {
                case BLOOD_PRESSURE:
                    if (vs.getBloodPressureSystolic() != null) {
                        values.add(vs.getBloodPressureSystolic().doubleValue());
                    }
                    break;
                case HEART_RATE:
                    if (vs.getHeartRate() != null) {
                        values.add(vs.getHeartRate().doubleValue());
                    }
                    break;
                case TEMPERATURE:
                    if (vs.getTemperature() != null) {
                        values.add(vs.getTemperature());
                    }
                    break;
                case RESPIRATORY_RATE:
                    if (vs.getRespiratoryRate() != null) {
                        values.add(vs.getRespiratoryRate().doubleValue());
                    }
                    break;
                case OXYGEN_SATURATION:
                    if (vs.getOxygenSaturation() != null) {
                        values.add(vs.getOxygenSaturation().doubleValue());
                    }
                    break;
            }
        }
        
        return values;
    }

    private Double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private Double calculateMedian(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();
        
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    private Double calculateStandardDeviation(List<Double> values) {
        double mean = calculateMean(values);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    private TrendDirection calculateTrend(List<Double> values) {
        if (values.size() < 2) {
            return TrendDirection.STABLE;
        }

        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double threshold = 0.1;
        
        if (slope > threshold) {
            return TrendDirection.INCREASING;
        } else if (slope < -threshold) {
            return TrendDirection.DECREASING;
        } else {
            return TrendDirection.STABLE;
        }
    }

    private VitalSignResponse convertToResponse(VitalSign vitalSign) {
        return VitalSignResponse.builder()
            .id(vitalSign.getId())
            .patientId(vitalSign.getPatientId())
            .measuredAt(vitalSign.getMeasuredAt())
            .bloodPressureSystolic(vitalSign.getBloodPressureSystolic())
            .bloodPressureDiastolic(vitalSign.getBloodPressureDiastolic())
            .heartRate(vitalSign.getHeartRate())
            .temperature(vitalSign.getTemperature())
            .respiratoryRate(vitalSign.getRespiratoryRate())
            .oxygenSaturation(vitalSign.getOxygenSaturation())
            .position(vitalSign.getPosition())
            .measurementMethod(vitalSign.getMeasurementMethod())
            .notes(vitalSign.getNotes())
            .recordedBy(vitalSign.getRecordedBy())
            .createdAt(vitalSign.getCreatedAt())
            .build();
    }
}
