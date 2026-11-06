package com.salespilot.salespilot.imports;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportService {

    private final ImportBatchRepository importBatchRepository;

    public ImportService(ImportBatchRepository importBatchRepository) {
        this.importBatchRepository = importBatchRepository;
    }

    @Transactional(readOnly = true)
    public ImportBatchResponse getById(Long id) {
        return importBatchRepository.findById(id)
                .map(ImportBatchResponse::from)
                .orElseThrow(() -> new ImportNotFoundException(id));
    }
}