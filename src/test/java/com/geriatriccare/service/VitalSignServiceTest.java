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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VitalSignService Unit Tests")
class VitalSignServiceTest {

    @Mock
    private VitalSignRepository vitalSignRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private VitalSignService vitalSignService;

    private UUID patientId;
    private UUID userId;
    private VitalSignRequest request;
    private VitalSign vitalSign;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        userId = UUID.randomUUID();

        request = new VitalSignRequest();
        request.setPatientId(patientId);
        request.setBloodPressureSystolic(120);
        request.setBloodPressureDiastolic(80);
        request.setHeartRate(72);
        request.setTemperature(36.6);
        request.setRespiratoryRate(16);
        request.setOxygenSaturation(98);

        vitalSign = createVitalSign(120, 80, 72, 36.6, 16, 98);
    }

    // ========================================
    // RECORD VITAL SIGN TESTS
    // ========================================

    @Test
    @DisplayName("Should record vital sign successfully")
    void recordVitalSign_Success() {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(vitalSignRepository.save(any(VitalSign.class))).thenReturn(vitalSign);

        // When
        VitalSignResponse response = vitalSignService.recordVitalSign(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getBloodPressureSystolic()).isEqualTo(120);
        assertThat(response.getBloodPressureDiastolic()).isEqualTo(80);
        assertThat(response.getHeartRate()).isEqualTo(72);
        assertThat(response.getTemperature()).isEqualTo(36.6);
        assertThat(response.getRecordedBy()).isEqualTo(userId);
        
        verify(vitalSignRepository).save(any(VitalSign.class));
        verify(securityUtil).getCurrentUserId();
    }

    @Test
    @DisplayName("Should set current timestamp when measuredAt is null")
    void recordVitalSign_AutoTimestamp() {
        // Given
        request.setMeasuredAt(null);
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(vitalSignRepository.save(any(VitalSign.class))).thenReturn(vitalSign);

        // When
        VitalSignResponse response = vitalSignService.recordVitalSign(request);

        // Then
        assertThat(response).isNotNull();
        verify(vitalSignRepository).save(argThat(vs -> 
            vs.getMeasuredAt() != null
        ));
    }

    @Test
    @DisplayName("Should record partial vital signs")
    void recordVitalSign_PartialData() {
        // Given - Only blood pressure provided
        request.setHeartRate(null);
        request.setTemperature(null);
        request.setRespiratoryRate(null);
        request.setOxygenSaturation(null);
        
        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(vitalSignRepository.save(any(VitalSign.class))).thenReturn(vitalSign);

        // When
        VitalSignResponse response = vitalSignService.recordVitalSign(request);

        // Then
        assertThat(response).isNotNull();
        verify(vitalSignRepository).save(any(VitalSign.class));
    }

    // ========================================
    // RETRIEVAL TESTS
    // ========================================

    @Test
    @DisplayName("Should get vital sign by ID")
    void getVitalSignById_Success() {
        // Given
        UUID id = vitalSign.getId();
        when(vitalSignRepository.findById(id)).thenReturn(Optional.of(vitalSign));

        // When
        VitalSignResponse response = vitalSignService.getVitalSignById(id);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        verify(vitalSignRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when vital sign not found")
    void getVitalSignById_NotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(vitalSignRepository.findById(id)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vitalSignService.getVitalSignById(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vital sign not found");
    }

    @Test
    @DisplayName("Should get all vital signs for patient")
    void getVitalSignsByPatient_Success() {
        // Given
        List<VitalSign> vitalSigns = Arrays.asList(vitalSign, createVitalSign(125, 82, 75, 36.8, 18, 97));
        when(vitalSignRepository.findByPatientIdAndDeletedFalseOrderByMeasuredAtDesc(patientId))
            .thenReturn(vitalSigns);

        // When
        List<VitalSignResponse> responses = vitalSignService.getVitalSignsByPatient(patientId);

        // Then
        assertThat(responses).hasSize(2);
        verify(vitalSignRepository).findByPatientIdAndDeletedFalseOrderByMeasuredAtDesc(patientId);
    }

    @Test
    @DisplayName("Should get vital signs with pagination")
    void getVitalSignsByPatientPaginated_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<VitalSign> page = new PageImpl<>(Arrays.asList(vitalSign));
        when(vitalSignRepository.findByPatientIdAndDeletedFalse(patientId, pageable))
            .thenReturn(page);

        // When
        Page<VitalSignResponse> result = vitalSignService.getVitalSignsByPatientPaginated(patientId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get latest vital sign")
    void getLatestVitalSign_Success() {
        // Given
        when(vitalSignRepository.findLatestByPatientId(patientId))
            .thenReturn(Optional.of(vitalSign));

        // When
        VitalSignResponse response = vitalSignService.getLatestVitalSign(patientId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(vitalSign.getId());
    }

    @Test
    @DisplayName("Should throw exception when no vital signs exist")
    void getLatestVitalSign_NotFound() {
        // Given
        when(vitalSignRepository.findLatestByPatientId(patientId))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vitalSignService.getLatestVitalSign(patientId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("No vital signs found");
    }

    // ========================================
    // STATISTICS & TREND TESTS
    // ========================================

    @Test
    @DisplayName("Should calculate statistics for increasing trend")
    void calculateStatistics_IncreasingTrend() {
        // Given - Increasing heart rate: 70, 75, 80, 85, 90
        List<VitalSign> vitalSigns = Arrays.asList(
            createVitalSignWithHeartRate(70),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(80),
            createVitalSignWithHeartRate(85),
            createVitalSignWithHeartRate(90)
        );

        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(vitalSigns);

        // When
        VitalSignStatistics stats = vitalSignService.calculateStatistics(
            patientId, VitalSignType.HEART_RATE, 30
        );

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getType()).isEqualTo(VitalSignType.HEART_RATE);
        assertThat(stats.getMean()).isEqualTo(80.0);
        assertThat(stats.getMin()).isEqualTo(70.0);
        assertThat(stats.getMax()).isEqualTo(90.0);
        assertThat(stats.getTrend()).isEqualTo(TrendDirection.INCREASING);
        assertThat(stats.getDataPoints()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should calculate statistics for decreasing trend")
    void calculateStatistics_DecreasingTrend() {
        // Given - Decreasing heart rate: 90, 85, 80, 75, 70
        List<VitalSign> vitalSigns = Arrays.asList(
            createVitalSignWithHeartRate(90),
            createVitalSignWithHeartRate(85),
            createVitalSignWithHeartRate(80),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(70)
        );

        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(vitalSigns);

        // When
        VitalSignStatistics stats = vitalSignService.calculateStatistics(
            patientId, VitalSignType.HEART_RATE, 30
        );

        // Then
        assertThat(stats.getTrend()).isEqualTo(TrendDirection.DECREASING);
    }

    @Test
    @DisplayName("Should calculate statistics for stable trend")
    void calculateStatistics_StableTrend() {
        // Given - Stable heart rate: 75, 75, 75, 75, 75
        List<VitalSign> vitalSigns = Arrays.asList(
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(75)
        );

        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(vitalSigns);

        // When
        VitalSignStatistics stats = vitalSignService.calculateStatistics(
            patientId, VitalSignType.HEART_RATE, 7
        );

        // Then
        assertThat(stats.getTrend()).isEqualTo(TrendDirection.STABLE);
        assertThat(stats.getStandardDeviation()).isLessThan(1.0);
    }

    @Test
    @DisplayName("Should calculate median correctly for odd count")
    void calculateStatistics_MedianOdd() {
        // Given - 5 values: 70, 75, 80, 85, 90
        List<VitalSign> vitalSigns = Arrays.asList(
            createVitalSignWithHeartRate(70),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(80),
            createVitalSignWithHeartRate(85),
            createVitalSignWithHeartRate(90)
        );

        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(vitalSigns);

        // When
        VitalSignStatistics stats = vitalSignService.calculateStatistics(
            patientId, VitalSignType.HEART_RATE, 30
        );

        // Then
        assertThat(stats.getMedian()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Should calculate median correctly for even count")
    void calculateStatistics_MedianEven() {
        // Given - 4 values: 70, 75, 85, 90 (median = (75+85)/2 = 80)
        List<VitalSign> vitalSigns = Arrays.asList(
            createVitalSignWithHeartRate(70),
            createVitalSignWithHeartRate(75),
            createVitalSignWithHeartRate(85),
            createVitalSignWithHeartRate(90)
        );

        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(vitalSigns);

        // When
        VitalSignStatistics stats = vitalSignService.calculateStatistics(
            patientId, VitalSignType.HEART_RATE, 30
        );

        // Then
        assertThat(stats.getMedian()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Should throw exception when no data for statistics")
    void calculateStatistics_NoData() {
        // Given
        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(Arrays.asList());

        // When/Then
        assertThatThrownBy(() -> 
            vitalSignService.calculateStatistics(patientId, VitalSignType.HEART_RATE, 30)
        ).isInstanceOf(ResourceNotFoundException.class)
         .hasMessageContaining("No vital signs found");
    }

    @Test
    @DisplayName("Should calculate statistics for temperature")
    void calculateStatistics_Temperature() {
        // Given
        List<VitalSign> vitalSigns = Arrays.asList(
            createVitalSignWithTemp(36.5),
            createVitalSignWithTemp(36.6),
            createVitalSignWithTemp(36.7),
            createVitalSignWithTemp(36.8),
            createVitalSignWithTemp(36.9)
        );

        when(vitalSignRepository.findByPatientIdAndDateRange(any(), any(), any()))
            .thenReturn(vitalSigns);

        // When
        VitalSignStatistics stats = vitalSignService.calculateStatistics(
            patientId, VitalSignType.TEMPERATURE, 7
        );

        // Then
        assertThat(stats.getType()).isEqualTo(VitalSignType.TEMPERATURE);
        assertThat(stats.getMean()).isEqualTo(36.7);
    }

    // ========================================
    // DELETE TESTS
    // ========================================

    @Test
    @DisplayName("Should soft delete vital sign")
    void deleteVitalSign_Success() {
        // Given
        UUID id = vitalSign.getId();
        when(vitalSignRepository.findById(id)).thenReturn(Optional.of(vitalSign));
        when(vitalSignRepository.save(any(VitalSign.class))).thenReturn(vitalSign);

        // When
        vitalSignService.deleteVitalSign(id);

        // Then
        verify(vitalSignRepository).save(argThat(vs -> vs.getDeleted() == true));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent vital sign")
    void deleteVitalSign_NotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(vitalSignRepository.findById(id)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vitalSignService.deleteVitalSign(id))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private VitalSign createVitalSign(Integer systolic, Integer diastolic, 
                                     Integer heartRate, Double temp, 
                                     Integer respRate, Integer spO2) {
        VitalSign vs = new VitalSign();
        vs.setId(UUID.randomUUID());
        vs.setPatientId(patientId);
        vs.setMeasuredAt(LocalDateTime.now());
        vs.setBloodPressureSystolic(systolic);
        vs.setBloodPressureDiastolic(diastolic);
        vs.setHeartRate(heartRate);
        vs.setTemperature(temp);
        vs.setRespiratoryRate(respRate);
        vs.setOxygenSaturation(spO2);
        vs.setRecordedBy(userId);
        vs.setCreatedAt(LocalDateTime.now());
        vs.setDeleted(false);
        return vs;
    }

    private VitalSign createVitalSignWithHeartRate(int heartRate) {
        VitalSign vs = new VitalSign();
        vs.setId(UUID.randomUUID());
        vs.setPatientId(patientId);
        vs.setHeartRate(heartRate);
        vs.setMeasuredAt(LocalDateTime.now());
        return vs;
    }

    private VitalSign createVitalSignWithTemp(double temp) {
        VitalSign vs = new VitalSign();
        vs.setId(UUID.randomUUID());
        vs.setPatientId(patientId);
        vs.setTemperature(temp);
        vs.setMeasuredAt(LocalDateTime.now());
        return vs;
    }
}
