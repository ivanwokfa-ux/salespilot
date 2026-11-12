package com.salespilot.salespilot.imports;

import com.salespilot.salespilot.common.web.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.net.URI;




@Tag(name = "Imports", description = "Importer des fichiers de ventes et consulter l'historique")
@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/{id}")
    public ImportBatchResponse getById(@PathVariable Long id) {
        return importService.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportBatchResponse> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "currency", defaultValue = "EUR") String currency) {
        ImportBatchResponse created = importService.create(file, currency);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
    @GetMapping("/{id}/errors")
    public PageResponse<ImportErrorResponse> listErrors(
            @PathVariable Long id,
            @ParameterObject   @PageableDefault(size = 50) Pageable pageable) {
        return importService.listErrors(id, pageable);
    }
    @GetMapping
    public PageResponse<ImportBatchResponse> list(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return importService.list(pageable);
    }

}