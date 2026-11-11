package com.salespilot.salespilot.reports;

import com.salespilot.salespilot.analytics.CurrencyMonthly;
import com.salespilot.salespilot.analytics.CurrencyOverview;
import com.salespilot.salespilot.analytics.MonthlyPoint;
import com.salespilot.salespilot.analytics.MonthlyResponse;
import com.salespilot.salespilot.analytics.OverviewResponse;
import com.salespilot.salespilot.analytics.TopProduct;
import com.salespilot.salespilot.analytics.TopProductsResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ExcelReportGenerator {

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int HEADER_ROW = 2;

    public byte[] generate(OverviewResponse overview, MonthlyResponse monthly, TopProductsResponse top) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            writeOverview(workbook.createSheet("Synthèse"), overview, styles);
            writeMonthly(workbook.createSheet("Mensuel"), monthly, styles);
            writeTopProducts(workbook.createSheet("Top produits"), top, styles);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Generation du rapport impossible", e);
        }
    }

    private void writeOverview(Sheet sheet, OverviewResponse data, Styles styles) {
        String[] headers = {"Devise", "Chiffre d'affaires HT", "Factures", "Panier moyen", "Unités vendues"};
        prepare(sheet, "Synthèse des ventes", data.from(), data.to(), headers, styles);
        int r = HEADER_ROW + 1;
        for (CurrencyOverview c : data.totals()) {
            Row row = sheet.createRow(r++);
            text(row, 0, c.currency());
            money(row, 1, c.revenue(), styles);
            integer(row, 2, c.invoiceCount(), styles);
            money(row, 3, c.averageBasket(), styles);
            integer(row, 4, c.unitsSold(), styles);
        }
        autoSize(sheet, headers.length);
    }

    private void writeMonthly(Sheet sheet, MonthlyResponse data, Styles styles) {
        String[] headers = {"Devise", "Mois", "Chiffre d'affaires HT", "Factures"};
        prepare(sheet, "Évolution mensuelle", data.from(), data.to(), headers, styles);
        int r = HEADER_ROW + 1;
        for (CurrencyMonthly series : data.series()) {
            for (MonthlyPoint point : series.months()) {
                Row row = sheet.createRow(r++);
                text(row, 0, series.currency());
                text(row, 1, point.month());
                money(row, 2, point.revenue(), styles);
                integer(row, 3, point.invoiceCount(), styles);
            }
        }
        autoSize(sheet, headers.length);
    }

    private void writeTopProducts(Sheet sheet, TopProductsResponse data, Styles styles) {
        String[] headers = {"Devise", "Rang", "Référence", "Désignation", "Unités vendues", "Chiffre d'affaires HT", "Part du CA"};
        prepare(sheet, "Produits les plus vendus", data.from(), data.to(), headers, styles);
        int r = HEADER_ROW + 1;
        for (TopProduct p : data.products()) {
            Row row = sheet.createRow(r++);
            text(row, 0, p.currency());
            integer(row, 1, p.rank(), styles);
            text(row, 2, p.productSku());
            text(row, 3, p.productName());
            integer(row, 4, p.unitsSold(), styles);
            money(row, 5, p.revenue(), styles);
            Cell share = row.createCell(6);
            if (p.sharePercent() != null) {
                share.setCellValue(p.sharePercent().doubleValue());
            }
            share.setCellStyle(styles.percent);
        }
        autoSize(sheet, headers.length);
    }

    /** Titre fusionne sur la largeur du tableau, ligne d'en-tetes, en-tetes figes au defilement. */
    private void prepare(Sheet sheet, String title, LocalDate from, LocalDate to, String[] headers, Styles styles) {
        Cell titleCell = sheet.createRow(0).createCell(0);
        titleCell.setCellValue(title + " du " + from.format(FR_DATE) + " au " + to.format(FR_DATE));
        titleCell.setCellStyle(styles.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

        Row headerRow = sheet.createRow(HEADER_ROW);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.header);
        }
        sheet.createFreezePane(0, HEADER_ROW + 1);
    }

    private void text(Row row, int col, String value) {
        row.createCell(col).setCellValue(value);
    }

    private void money(Row row, int col, BigDecimal value, Styles styles) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        cell.setCellStyle(styles.money);
    }

    private void integer(Row row, int col, long value, Styles styles) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(styles.integer);
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /** Les styles sont crees une seule fois par classeur, puis reutilises. */
    private static final class Styles {
        final CellStyle title;
        final CellStyle header;
        final CellStyle money;
        final CellStyle integer;
        final CellStyle percent;

        Styles(Workbook workbook) {
            DataFormat format = workbook.createDataFormat();

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            title = workbook.createCellStyle();
            title.setFont(titleFont);

            Font bold = workbook.createFont();
            bold.setBold(true);
            header = workbook.createCellStyle();
            header.setFont(bold);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setBorderBottom(BorderStyle.THIN);

            money = workbook.createCellStyle();
            money.setDataFormat(format.getFormat("#,##0.00"));

            integer = workbook.createCellStyle();
            integer.setDataFormat(format.getFormat("#,##0"));

            percent = workbook.createCellStyle();
            percent.setDataFormat(format.getFormat("0.00\" %\""));
        }
    }
}