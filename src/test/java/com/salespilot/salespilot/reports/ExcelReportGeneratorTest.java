package com.salespilot.salespilot.reports;

import com.salespilot.salespilot.analytics.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelReportGeneratorTest {

    private final ExcelReportGenerator generator = new ExcelReportGenerator();

    @Test
    void genereUnClasseurDeTroisOngletsAvecLesBonnesValeurs() throws IOException {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        OverviewResponse overview = new OverviewResponse(from, to, List.of(
                new CurrencyOverview("XAF", new BigDecimal("206737.20"), 2, new BigDecimal("103368.60"), 208)));
        MonthlyResponse monthly = new MonthlyResponse(from, to, List.of(
                new CurrencyMonthly("XAF", List.of(new MonthlyPoint("2026-01", new BigDecimal("206737.20"), 2)))));
        TopProductsResponse top = new TopProductsResponse(from, to, List.of(
                new TopProduct("XAF", 1, "01011450", "NEVADA 450 g", 200, new BigDecimal("196746.00"), new BigDecimal("95.17"))));

        byte[] content = generator.generate(overview, monthly, top);

        // On relit le fichier genere, comme le ferait Excel
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
            assertThat(workbook.getSheetName(0)).isEqualTo("Synthèse");

            Row synthese = workbook.getSheet("Synthèse").getRow(3);
            assertThat(synthese.getCell(0).getStringCellValue()).isEqualTo("XAF");
            assertThat(synthese.getCell(1).getNumericCellValue()).isEqualTo(206737.20);

            Row topRow = workbook.getSheet("Top produits").getRow(3);
            assertThat(topRow.getCell(3).getStringCellValue()).isEqualTo("NEVADA 450 g");
        }
    }
}