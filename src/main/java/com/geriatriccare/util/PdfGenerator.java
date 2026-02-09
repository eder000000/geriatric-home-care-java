package com.geriatriccare.util;

import com.geriatriccare.dto.report.AdherenceReportResponse;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * PdfGenerator
 * Generates formatted PDF reports for care plan adherence
 * Sprint 8 - US-7.1 (GCARE-714)
 */
@Component
@Slf4j
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public byte[] generateAdherenceReportPdf(AdherenceReportResponse report) {
        log.info("Generating PDF for report: {}", report.getReportId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            addTitle(document, report);

            // Add report metadata
            addMetadata(document, report);

            // Add summary section
            addSummary(document, report);

            // Add metrics table
            addMetricsTable(document, report);

            // Add category breakdown
            if (report.getCategoryBreakdown() != null && !report.getCategoryBreakdown().isEmpty()) {
                addCategoryBreakdown(document, report);
            }

            // Add footer
            addFooter(document);

            document.close();
            log.info("PDF generated successfully: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public String savePdfToFile(byte[] pdfData, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfData);
            log.info("PDF saved to: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Error saving PDF to file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save PDF to file", e);
        }
    }

    private void addTitle(Document document, AdherenceReportResponse report) {
        Paragraph title = new Paragraph("Care Plan Adherence Report")
            .setFontSize(24)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        document.add(title);

        Paragraph patientName = new Paragraph("Patient: " + report.getPatientName())
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10);
        document.add(patientName);
    }

    private void addMetadata(Document document, AdherenceReportResponse report) {
        String period = String.format("%s to %s",
            report.getStartDate().format(DATE_FORMATTER),
            report.getEndDate().format(DATE_FORMATTER)
        );

        Paragraph metadata = new Paragraph()
            .add("Report Period: " + period + "\n")
            .add("Generated: " + report.getGeneratedAt().format(DATETIME_FORMATTER) + "\n")
            .add("Report ID: " + report.getReportId())
            .setFontSize(10)
            .setMarginBottom(20);
        document.add(metadata);
    }

    private void addSummary(Document document, AdherenceReportResponse report) {
        // Summary header
        Paragraph summaryHeader = new Paragraph("Executive Summary")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(summaryHeader);

        // Summary text
        String summaryText = report.getReportSummary() != null ? 
            report.getReportSummary() : 
            generateDefaultSummary(report);

        Paragraph summary = new Paragraph(summaryText)
            .setFontSize(11)
            .setMarginBottom(20);
        document.add(summary);
    }

    private void addMetricsTable(Document document, AdherenceReportResponse report) {
        // Metrics header
        Paragraph metricsHeader = new Paragraph("Key Metrics")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(metricsHeader);

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
            .useAllAvailableWidth()
            .setMarginBottom(20);

        // Header row
        table.addHeaderCell(createHeaderCell("Metric"));
        table.addHeaderCell(createHeaderCell("Value"));

        // Data rows
        table.addCell(createCell("Total Tasks"));
        table.addCell(createCell(String.valueOf(report.getTotalTasks())));

        table.addCell(createCell("Completed Tasks"));
        table.addCell(createCell(String.valueOf(report.getCompletedTasks())));

        table.addCell(createCell("Missed Tasks"));
        table.addCell(createCell(String.valueOf(report.getMissedTasks())));

        table.addCell(createCell("Pending Tasks"));
        table.addCell(createCell(String.valueOf(report.getPendingTasks())));

        table.addCell(createCell("Adherence Rate"));
        table.addCell(createCell(String.format("%.1f%%", report.getAdherencePercentage())));

        table.addCell(createCell("Trend"));
        table.addCell(createCell(report.getTrend()));

        document.add(table);
    }

    private void addCategoryBreakdown(Document document, AdherenceReportResponse report) {
        // Category header
        Paragraph categoryHeader = new Paragraph("Adherence by Category")
            .setFontSize(16)
            .setBold()
            .setMarginBottom(10);
        document.add(categoryHeader);

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 2}))
            .useAllAvailableWidth()
            .setMarginBottom(20);

        // Header row
        table.addHeaderCell(createHeaderCell("Category"));
        table.addHeaderCell(createHeaderCell("Total"));
        table.addHeaderCell(createHeaderCell("Completed"));
        table.addHeaderCell(createHeaderCell("Rate"));

        // Data rows
        for (AdherenceReportResponse.CategoryAdherence category : report.getCategoryBreakdown()) {
            table.addCell(createCell(category.getCategory()));
            table.addCell(createCell(String.valueOf(category.getTotal())));
            table.addCell(createCell(String.valueOf(category.getCompleted())));
            table.addCell(createCell(String.format("%.1f%%", category.getAdherenceRate())));
        }

        document.add(table);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph()
            .add("\n\n")
            .add("This report is confidential and intended for healthcare professionals only.\n")
            .add("Generated by Geriatric Home Care System")
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY);
        document.add(footer);
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setBold())
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(5);
    }

    private Cell createCell(String text) {
        return new Cell()
            .add(new Paragraph(text))
            .setPadding(5);
    }

    private String generateDefaultSummary(AdherenceReportResponse report) {
        return String.format(
            "Patient %s demonstrated %.1f%% adherence over the reporting period. " +
            "%d of %d care tasks were completed, with %d tasks missed. " +
            "The adherence trend is %s.",
            report.getPatientName(),
            report.getAdherencePercentage(),
            report.getCompletedTasks(),
            report.getTotalTasks(),
            report.getMissedTasks(),
            report.getTrend().toLowerCase()
        );
    }
}
