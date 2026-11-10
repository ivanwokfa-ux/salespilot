package com.salespilot.salespilot.imports;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {

    Page<ImportError> findByImportBatchIdOrderByLineNumber(Long importBatchId, Pageable pageable);
}