package com.salespilot.salespilot.imports;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "import_batch")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_hash", nullable = false, unique = true, length = 64)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImportStatus status;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "imported_rows", nullable = false)
    private int importedRows;

    @Column(name = "rejected_rows", nullable = false)
    private int rejectedRows;

    @Column(name = "duplicate_rows", nullable = false)
    private int duplicateRows;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructeur vide exige par JPA
    protected ImportBatch() {
    }

    public ImportBatch(String fileName, String fileHash) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.status = ImportStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void markCompleted(int totalRows, int importedRows, int rejectedRows, int duplicateRows) {
        this.totalRows = totalRows;
        this.importedRows = importedRows;
        this.rejectedRows = rejectedRows;
        this.duplicateRows = duplicateRows;
        this.status = ImportStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = ImportStatus.FAILED;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFileHash() { return fileHash; }
    public ImportStatus getStatus() { return status; }
    public int getTotalRows() { return totalRows; }
    public int getImportedRows() { return importedRows; }
    public int getRejectedRows() { return rejectedRows; }
    public int getDuplicateRows() { return duplicateRows; }
    public Instant getCreatedAt() { return createdAt; }
}