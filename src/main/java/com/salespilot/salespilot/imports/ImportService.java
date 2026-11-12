package com.salespilot.salespilot.imports;

import com.salespilot.salespilot.common.web.PageResponse;
import com.salespilot.salespilot.imports.parsing.*;
import com.salespilot.salespilot.sales.Sale;
import com.salespilot.salespilot.sales.SaleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.salespilot.salespilot.imports.parsing.ExcelTableReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

@Service
public class ImportService {

    private final ImportBatchRepository importBatchRepository;
    private final ImportErrorRepository importErrorRepository;
    private final SaleRepository saleRepository;
    private final CsvTableReader csvTableReader;
    private final ColumnMapper columnMapper;
    private final SaleRowMapper saleRowMapper;
    private final ExcelTableReader excelTableReader;

    public ImportService(ImportBatchRepository importBatchRepository,
                         ImportErrorRepository importErrorRepository,
                         SaleRepository saleRepository,
                         CsvTableReader csvTableReader,
                         ExcelTableReader excelTableReader,
                         ColumnMapper columnMapper,
                         SaleRowMapper saleRowMapper) {
        this.importBatchRepository = importBatchRepository;
        this.importErrorRepository = importErrorRepository;
        this.saleRepository = saleRepository;
        this.csvTableReader = csvTableReader;
        this.columnMapper = columnMapper;
        this.excelTableReader = excelTableReader;
        this.saleRowMapper = saleRowMapper;
    }

    @Transactional(readOnly = true)
    public ImportBatchResponse getById(Long id) {
        return importBatchRepository.findById(id)
                .map(ImportBatchResponse::from)
                .orElseThrow(() -> new ImportNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ImportBatchResponse> list(Pageable pageable) {
        return PageResponse.from(importBatchRepository.findAll(pageable).map(ImportBatchResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ImportErrorResponse> listErrors(Long importId, Pageable pageable) {
        if (!importBatchRepository.existsById(importId)) {
            throw new ImportNotFoundException(importId);
        }
        return PageResponse.from(
                importErrorRepository.findByImportBatchIdOrderByLineNumber(importId, pageable)
                        .map(ImportErrorResponse::from));
    }

    @Transactional
    public ImportBatchResponse create(MultipartFile file, String defaultCurrency) {
        // 1. Controles sur le fichier
        if (file.isEmpty()) {
            throw new InvalidImportFileException("Le fichier est vide");
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "sans-nom";
        byte[] content = readBytes(file);
        String hash = sha256(content);
        if (importBatchRepository.existsByFileHash(hash)) {
            throw new DuplicateImportException(fileName);
        }

        // 2. Lecture et reconnaissance des colonnes (avant tout enregistrement)
        RawTable table = readTable(content);
        ColumnMapping mapping = columnMapper.map(table.headers());

        // 3. Enregistrement de l'import
        ImportBatch batch;
        try {
            batch = importBatchRepository.saveAndFlush(new ImportBatch(fileName, hash));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateImportException(fileName);
        }

        // 4. Traitement ligne par ligne
        List<Sale> sales = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        int duplicates = 0;

        for (RawRow row : table.rows()) {
            try {
                Sale sale = saleRowMapper.toSale(row, mapping, batch, defaultCurrency);
                String key = sale.getAgency() + "|" + sale.getOrderRef() + "|" + sale.getProductSku();
                boolean duplicate = !seenKeys.add(key)
                        || saleRepository.existsByAgencyAndOrderRefAndProductSku(
                        sale.getAgency(), sale.getOrderRef(), sale.getProductSku());
                if (duplicate) {
                    duplicates++;
                } else {
                    sales.add(sale);
                }
            } catch (InvalidValueException e) {
                errors.add(new ImportError(batch, row.lineNumber(), String.join(";", row.values()), e.getMessage()));
            }
        }

        // 5. Enregistrement des resultats
        saleRepository.saveAll(sales);
        importErrorRepository.saveAll(errors);
        batch.markCompleted(table.rows().size(), sales.size(), errors.size(), duplicates);

        return ImportBatchResponse.from(batch);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du fichier impossible", e);
        }
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
    private RawTable readTable(byte[] content) {
        if (startsWith(content, 0x50, 0x4B, 0x03, 0x04)) {       // "PK.." : un .xlsx est une archive ZIP
            return excelTableReader.read(content);
        }
        if (startsWith(content, 0xD0, 0xCF, 0x11, 0xE0)) {       // ancien format Excel 97-2003
            throw new InvalidImportFileException(
                    "Format .xls non pris en charge : enregistrez le fichier au format .xlsx ou .csv");
        }
        return csvTableReader.read(content);
    }

    private boolean startsWith(byte[] content, int... signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((content[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}