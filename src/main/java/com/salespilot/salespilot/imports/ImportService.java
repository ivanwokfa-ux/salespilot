package com.salespilot.salespilot.imports;

import com.salespilot.salespilot.common.web.PageResponse;
import com.salespilot.salespilot.imports.parsing.ColumnMapper;
import com.salespilot.salespilot.imports.parsing.ColumnMapping;
import com.salespilot.salespilot.imports.parsing.CsvTableReader;
import com.salespilot.salespilot.imports.parsing.InvalidValueException;
import com.salespilot.salespilot.imports.parsing.RawRow;
import com.salespilot.salespilot.imports.parsing.RawTable;
import com.salespilot.salespilot.imports.parsing.SaleRowMapper;
import com.salespilot.salespilot.sales.Sale;
import com.salespilot.salespilot.sales.SaleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public ImportService(ImportBatchRepository importBatchRepository,
                         ImportErrorRepository importErrorRepository,
                         SaleRepository saleRepository,
                         CsvTableReader csvTableReader,
                         ColumnMapper columnMapper,
                         SaleRowMapper saleRowMapper) {
        this.importBatchRepository = importBatchRepository;
        this.importErrorRepository = importErrorRepository;
        this.saleRepository = saleRepository;
        this.csvTableReader = csvTableReader;
        this.columnMapper = columnMapper;
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
        RawTable table = csvTableReader.read(content);
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
}