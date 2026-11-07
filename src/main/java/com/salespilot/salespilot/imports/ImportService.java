package com.salespilot.salespilot.imports;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
    @Transactional
    public ImportBatchResponse create(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidImportFileException("Le fichier est vide");
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "sans-nom";
        String hash = sha256(file);

        if (importBatchRepository.existsByFileHash(hash)) {
            throw new DuplicateImportException(fileName);
        }

        ImportBatch saved = importBatchRepository.save(new ImportBatch(fileName, hash));
        return ImportBatchResponse.from(saved);
    }

    private String sha256(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du fichier impossible", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}