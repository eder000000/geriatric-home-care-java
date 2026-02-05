package com.geriatriccare.dto.vitalsign;

import com.geriatriccare.enums.TrendDirection;
import com.geriatriccare.enums.VitalSignType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignStatistics {
    private VitalSignType type;
    private Double mean;
    private Double median;
    private Double standardDeviation;
    private Double min;
    private Double max;
    private TrendDirection trend;
    private Integer dataPoints;
}
