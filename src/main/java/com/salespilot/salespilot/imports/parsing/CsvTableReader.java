package com.salespilot.salespilot.imports.parsing;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.salespilot.salespilot.imports.InvalidImportFileException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvTableReader {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    public RawTable read(byte[] content) {
        String text = decode(content);
        CSVParser parser = new CSVParserBuilder().withSeparator(detectSeparator(text)).build();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(text)).withCSVParser(parser).build()) {
            String[] header = reader.readNext();
            if (header == null) {
                throw new InvalidImportFileException("Le fichier ne contient aucune ligne");
            }
            List<RawRow> rows = new ArrayList<>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isBlank(line)) {
                    continue;
                }
                rows.add(new RawRow((int) reader.getLinesRead(), trimAll(line)));
            }
            return new RawTable(trimAll(header), rows);
        } catch (IOException | CsvValidationException e) {
            throw new InvalidImportFileException("Fichier CSV illisible : " + e.getMessage());
        }
    }

    /** UTF-8 si le fichier est valide en UTF-8, sinon Windows-1252 (exports Excel francais). */
    private String decode(byte[] content) {
        try {
            String text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
            return text.startsWith("\uFEFF") ? text.substring(1) : text;
        } catch (CharacterCodingException e) {
            return new String(content, WINDOWS_1252);
        }
    }

    /** Choisit le separateur le plus frequent sur la premiere ligne. */
    private char detectSeparator(String text) {
        int end = text.indexOf('\n');
        String firstLine = end >= 0 ? text.substring(0, end) : text;
        char best = ',';
        long bestCount = 0;
        for (char candidate : new char[]{';', ',', '\t'}) {
            long count = firstLine.chars().filter(c -> c == candidate).count();
            if (count > bestCount) {
                best = candidate;
                bestCount = count;
            }
        }
        return best;
    }

    private boolean isBlank(String[] line) {
        return Arrays.stream(line).allMatch(value -> value == null || value.isBlank());
    }

    private List<String> trimAll(String[] values) {
        return Arrays.stream(values).map(value -> value == null ? "" : value.trim()).toList();
    }
}