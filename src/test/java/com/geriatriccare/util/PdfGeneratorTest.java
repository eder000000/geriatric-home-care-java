package com.geriatriccare.util;

import com.geriatriccare.dto.report.AdherenceReportResponse;
import com.geriatriccare.enums.ReportType;
import com.geriatriccare.enums.TimePeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PdfGenerator Tests")
class PdfGeneratorTest {

    private final PdfGenerator pdfGenerator = new PdfGenerator();

    @Test
    @DisplayName("Should generate PDF successfully")
    void generatePdf_Success() {
        // Given
        AdherenceReportResponse report = AdherenceReportResponse.builder()
            .reportId(UUID.randomUUID())
            .patientId(UUID.randomUUID())
            .patientName("John Doe")
            .reportType(ReportType.CARE_PLAN_ADHERENCE)
            .timePeriod(TimePeriod.LAST_30_DAYS)
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now())
            .totalTasks(20)
            .completedTasks(15)
            .missedTasks(5)
            .pendingTasks(0)
            .adherencePercentage(75.0)
            .trend("STABLE")
            .categoryBreakdown(Collections.emptyList())
            .reportTitle("Test Report")
            .reportSummary("Test summary")
            .generatedAt(LocalDateTime.now())
            .build();

        // When
        byte[] pdf = pdfGenerator.generateAdherenceReportPdf(report);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
    }
}
