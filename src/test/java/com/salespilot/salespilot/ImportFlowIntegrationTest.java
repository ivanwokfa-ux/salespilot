package com.salespilot.salespilot;

import com.salespilot.salespilot.analytics.AnalyticsService;
import com.salespilot.salespilot.analytics.CurrencyOverview;
import com.salespilot.salespilot.analytics.TopProduct;
import com.salespilot.salespilot.imports.DuplicateImportException;
import com.salespilot.salespilot.imports.ImportBatchResponse;
import com.salespilot.salespilot.imports.ImportService;
import com.salespilot.salespilot.imports.ImportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ImportFlowIntegrationTest extends AbstractIntegrationTest {

    private static final LocalDate JANUARY_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate JANUARY_END = LocalDate.of(2026, 1, 31);

    @Autowired
    private ImportService importService;

    @Autowired
    private AnalyticsService analyticsService;

    private byte[] demoContent() throws IOException {
        return Files.readAllBytes(Path.of("samples/ventes_demo.csv"));
    }

    private MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("file", name, "text/csv", content);
    }

    @Test
    void importeLeFichierDeDemonstration() throws IOException {
        ImportBatchResponse result = importService.create(file("ventes_demo.csv", demoContent()), "XAF");

        assertThat(result.status()).isEqualTo(ImportStatus.COMPLETED);
        assertThat(result.totalRows()).isEqualTo(8);
        assertThat(result.importedRows()).isEqualTo(4);
        assertThat(result.rejectedRows()).isEqualTo(3);
        assertThat(result.duplicateRows()).isEqualTo(1);
        assertThat(importService.listErrors(result.id(), PageRequest.of(0, 50)).content()).hasSize(3);
    }

    @Test
    void refuseUnFichierDejaImporte() throws IOException {
        importService.create(file("ventes_demo.csv", demoContent()), "XAF");

        assertThatThrownBy(() -> importService.create(file("copie.csv", demoContent()), "XAF"))
                .isInstanceOf(DuplicateImportException.class);
    }

    @Test
    void detecteLesVentesDejaPresentesEnBase() throws IOException {
        importService.create(file("ventes_demo.csv", demoContent()), "XAF");

        // Meme fichier + une nouvelle ligne : l'empreinte change, mais les ventes existent deja
        String content = new String(demoContent(), StandardCharsets.UTF_8).stripTrailing()
                + "\nFYB00006;20/01/26;CL005;Client E;01011450;NEVADA 450 g;3;983,73\n";
        ImportBatchResponse second = importService.create(
                file("ventes_demo_v2.csv", content.getBytes(StandardCharsets.UTF_8)), "XAF");

        assertThat(second.totalRows()).isEqualTo(9);
        assertThat(second.importedRows()).isEqualTo(1);   // seule la nouvelle ligne
        assertThat(second.duplicateRows()).isEqualTo(5);  // 4 deja en base + 1 doublon interne
        assertThat(second.rejectedRows()).isEqualTo(3);
    }

    @Test
    void calculeLesIndicateursApresImport() throws IOException {
        importService.create(file("ventes_demo.csv", demoContent()), "XAF");

        CurrencyOverview overview = analyticsService.overview(JANUARY_START, JANUARY_END).totals().get(0);
        assertThat(overview.currency()).isEqualTo("XAF");
        assertThat(overview.revenue()).isEqualByComparingTo("206737.20");
        assertThat(overview.invoiceCount()).isEqualTo(2);
        assertThat(overview.averageBasket()).isEqualByComparingTo("103368.60");

        TopProduct first = analyticsService.topProducts(JANUARY_START, JANUARY_END, 10).products().get(0);
        assertThat(first.productName()).isEqualTo("NEVADA 450 g");
        assertThat(first.sharePercent()).isEqualByComparingTo("95.17");
    }
}