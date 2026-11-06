package com.salespilot.salespilot.imports;

import java.time.Instant;

public record ImportBatchResponse(
        Long id,
        String fileName,
        ImportStatus status,
        int totalRows,
        int importedRows,
        int rejectedRows,
        int duplicateRows,
        Instant createdAt
) {
    public static ImportBatchResponse from(ImportBatch batch) {
        return new ImportBatchResponse(
                batch.getId(),
                batch.getFileName(),
                batch.getStatus(),
                batch.getTotalRows(),
                batch.getImportedRows(),
                batch.getRejectedRows(),
                batch.getDuplicateRows(),
                batch.getCreatedAt()
        );
    }
}