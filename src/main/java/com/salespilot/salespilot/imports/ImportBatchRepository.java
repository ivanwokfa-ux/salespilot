package com.salespilot.salespilot.imports;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

    boolean existsByFileHash(String fileHash);
}