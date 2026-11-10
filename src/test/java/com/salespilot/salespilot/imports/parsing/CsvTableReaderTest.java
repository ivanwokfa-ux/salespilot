package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.InvalidImportFileException;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvTableReaderTest {

    private final CsvTableReader reader = new CsvTableReader();

    @Test
    void litUnFichierAvecPointVirgule() {
        byte[] csv = "Piece;Date;Article\nFYB001;31/01/2026;NEVADA 450 g\n".getBytes(StandardCharsets.UTF_8);

        RawTable table = reader.read(csv);

        assertThat(table.headers()).containsExactly("Piece", "Date", "Article");
        assertThat(table.rows()).hasSize(1);
        assertThat(table.rows().get(0).values()).containsExactly("FYB001", "31/01/2026", "NEVADA 450 g");
    }

    @Test
    void litUnFichierAvecVirgule() {
        byte[] csv = "order_ref,quantity\nCMD-1,2\nCMD-2,3\n".getBytes(StandardCharsets.UTF_8);

        assertThat(reader.read(csv).rows()).hasSize(2);
    }

    @Test
    void litUnFichierWindows1252() {
        byte[] csv = "Désignation;Qté\nCafé;2\n".getBytes(Charset.forName("windows-1252"));

        assertThat(reader.read(csv).headers()).containsExactly("Désignation", "Qté");
    }

    @Test
    void ignoreLesLignesVides() {
        byte[] csv = "a;b\n1;2\n;\n3;4\n".getBytes(StandardCharsets.UTF_8);

        assertThat(reader.read(csv).rows()).hasSize(2);
    }

    @Test
    void refuseUnFichierVide() {
        assertThatThrownBy(() -> reader.read(new byte[0]))
                .isInstanceOf(InvalidImportFileException.class);
    }
}