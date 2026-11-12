package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.InvalidImportFileException;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelTableReader {

    public RawTable read(byte[] content) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = null;
            List<RawRow> rows = new ArrayList<>();

            for (Row row : sheet) {
                int width = headers == null ? row.getLastCellNum() : headers.size();
                List<String> values = readRow(row, width);
                if (values.stream().allMatch(String::isBlank)) {
                    continue;
                }
                if (headers == null) {
                    headers = values;
                } else {
                    rows.add(new RawRow(row.getRowNum() + 1, values));
                }
            }

            if (headers == null) {
                throw new InvalidImportFileException("La premiere feuille du fichier Excel est vide");
            }
            return new RawTable(headers, rows);
        } catch (IOException | IllegalArgumentException | POIXMLException e) {
            throw new InvalidImportFileException("Fichier Excel illisible : " + e.getMessage());
        }
    }

    private List<String> readRow(Row row, int width) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            values.add(cell == null ? "" : text(cell));
        }
        return values;
    }

    /** Convertit une cellule en texte lisible par ValueParser, selon son type. */
    private String text(Cell cell) {
        CellType type = cell.getCellType() == CellType.FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();

        return switch (type) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}