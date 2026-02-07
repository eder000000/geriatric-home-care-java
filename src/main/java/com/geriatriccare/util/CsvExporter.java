package com.geriatriccare.util;

import com.geriatriccare.dto.report.AdherenceReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

/**
 * CsvExporter
 * Exports reports to CSV format
 * Sprint 8 - US-7.1 (GCARE-714)
 */
@Component
@Slf4j
public class CsvExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CSV_SEPARATOR = ",";

    public byte[] generateAdherenceReportCsv(AdherenceReportResponse report) {
        log.info("Generating CSV for report: {}", report.getReportId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {

            // Write header section
            writer.println("Care Plan Adherence Report");
            writer.println("Patient," + escapeCsv(report.getPatientName()));
            writer.println("Report Period," + report.getStartDate().format(DATE_FORMATTER) + 
                          " to " + report.getEndDate().format(DATE_FORMATTER));
            writer.println("Generated," + report.getGeneratedAt().format(DATE_FORMATTER));
            writer.println();

            // Write metrics section
            writer.println("Key Metrics");
            writer.println("Metric,Value");
            writer.println("Total Tasks," + report.getTotalTasks());
            writer.println("Completed Tasks," + report.getCompletedTasks());
            writer.println("Missed Tasks," + report.getMissedTasks());
            writer.println("Pending Tasks," + report.getPendingTasks());
            writer.println("Adherence Rate," + String.format("%.1f%%", report.getAdherencePercentage()));
            writer.println("Trend," + report.getTrend());
            writer.println();

            // Write category breakdown
            if (report.getCategoryBreakdown() != null && !report.getCategoryBreakdown().isEmpty()) {
                writer.println("Category Breakdown");
                writer.println("Category,Total,Completed,Adherence Rate");
                
                for (AdherenceReportResponse.CategoryAdherence category : report.getCategoryBreakdown()) {
                    writer.println(String.format("%s,%d,%d,%.1f%%",
                        escapeCsv(category.getCategory()),
                        category.getTotal(),
                        category.getCompleted(),
                        category.getAdherenceRate()
                    ));
                }
            }

            writer.flush();
            log.info("CSV generated successfully: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
