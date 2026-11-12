package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.InvalidImportFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelTableReaderTest {

    private final ExcelTableReader reader = new ExcelTableReader();

    @Test
    void litLesTextesDatesEtNombres() throws IOException {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Ventes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("N° pièce");
            header.createCell(1).setCellValue("Date");
            header.createCell(2).setCellValue("Qté");
            header.createCell(3).setCellValue("Prix unitaire");

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("FYB00001");
            Cell date = row.createCell(1);
            date.setCellValue(LocalDate.of(2026, 1, 6));
            date.setCellStyle(dateStyle);
            row.createCell(2).setCellValue(120);
            row.createCell(3).setCellValue(983.73);

            sheet.createRow(2);   // une ligne vide, qui doit etre ignoree

            workbook.write(out);
            content = out.toByteArray();
        }

        RawTable table = reader.read(content);

        assertThat(table.headers()).containsExactly("N° pièce", "Date", "Qté", "Prix unitaire");
        assertThat(table.rows()).hasSize(1);
        assertThat(table.rows().get(0).values()).containsExactly("FYB00001", "2026-01-06", "120", "983.73");
        assertThat(table.rows().get(0).lineNumber()).isEqualTo(2);
    }

    @Test
    void refuseUnFichierExcelCorrompu() {
        byte[] corrupted = {0x50, 0x4B, 0x03, 0x04, 1, 2, 3};

        assertThatThrownBy(() -> reader.read(corrupted))
                .isInstanceOf(InvalidImportFileException.class);
    }
}