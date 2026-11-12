package com.salespilot.salespilot.reports;

import com.salespilot.salespilot.analytics.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ReportService {

    private static final int TOP_PRODUCTS_IN_REPORT = 10;

    private final AnalyticsService analyticsService;
    private final ExcelReportGenerator excelReportGenerator;

    public ReportService(AnalyticsService analyticsService, ExcelReportGenerator excelReportGenerator) {
        this.analyticsService = analyticsService;
        this.excelReportGenerator = excelReportGenerator;
    }

    @Transactional(readOnly = true)
    public byte[] salesReport(LocalDate from, LocalDate to) {
        return excelReportGenerator.generate(
                analyticsService.overview(from, to),
                analyticsService.monthly(from, to),
                analyticsService.topProducts(from, to, TOP_PRODUCTS_IN_REPORT));
    }
}