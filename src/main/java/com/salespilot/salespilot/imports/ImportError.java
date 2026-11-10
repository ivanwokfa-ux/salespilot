package com.salespilot.salespilot.imports;

import jakarta.persistence.*;

@Entity
@Table(name = "import_error")
public class ImportError {

    private static final int MAX_MESSAGE_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_batch_id", nullable = false)
    private ImportBatch importBatch;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Column(name = "raw_line", columnDefinition = "text")
    private String rawLine;

    @Column(name = "error_message", nullable = false, length = MAX_MESSAGE_LENGTH)
    private String errorMessage;

    protected ImportError() {
    }

    public ImportError(ImportBatch importBatch, int lineNumber, String rawLine, String errorMessage) {
        this.importBatch = importBatch;
        this.lineNumber = lineNumber;
        this.rawLine = rawLine;
        this.errorMessage = errorMessage.length() > MAX_MESSAGE_LENGTH
                ? errorMessage.substring(0, MAX_MESSAGE_LENGTH)
                : errorMessage;
    }

    public Long getId() { return id; }
    public int getLineNumber() { return lineNumber; }
    public String getRawLine() { return rawLine; }
    public String getErrorMessage() { return errorMessage; }
}