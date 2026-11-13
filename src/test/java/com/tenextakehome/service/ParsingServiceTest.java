package com.tenextakehome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.service.parser.CsvParser;
import com.tenextakehome.service.parser.JsonParser;
import com.tenextakehome.service.parser.XmlParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParsingServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ParsingService parsingService = new ParsingService(java.util.List.of(
            new XmlParser(objectMapper),
            new JsonParser(objectMapper),
            new CsvParser(objectMapper)), objectMapper);

    @Test
    void parsesXmlLogToJson() {
        String output = parsingService.parseLog(
                "<log><level>INFO</level><message>started</message></log>", ParserType.XML);

        assertEquals("{\"log\":{\"level\":\"INFO\",\"message\":\"started\"}}", output);
    }

    @Test
    void parsesAndNormalizesJsonLog() {
        String output = parsingService.parseLog("{ \"level\" : \"INFO\" }", ParserType.JSON);

        assertEquals("{\"level\":\"INFO\"}", output);
    }

    @Test
    void removesConfiguredFieldsFromParsedJsonObjectsRecursively() {
        String output = parsingService.parseLog(
                "{\"level\":\"INFO\",\"metadata\":{\"token\":\"secret\",\"host\":\"app\"},"
                        + "\"events\":[{\"token\":\"nested-secret\",\"message\":\"started\"}]}",
                ParserType.JSON,
                java.util.List.of("token"));

        assertEquals("{\"level\":\"INFO\",\"metadata\":{\"host\":\"app\"},"
                + "\"events\":[{\"message\":\"started\"}]}", output);
    }

    @Test
    void removesConfiguredFieldsFromConvertedXml() {
        String output = parsingService.parseLog(
                "<log><level>INFO</level><token>secret</token></log>",
                ParserType.XML,
                java.util.List.of("token"));

        assertEquals("{\"log\":{\"level\":\"INFO\"}}", output);
    }

    @Test
    void leavesParsedOutputUnchangedWhenNoFieldsAreConfiguredForRemoval() {
        String output = parsingService.parseLog(
                "{\"level\":\"INFO\",\"message\":\"started\"}", ParserType.JSON, java.util.List.of());

        assertEquals("{\"level\":\"INFO\",\"message\":\"started\"}", output);
    }

    @Test
    void parsesCsvLogWithQuotedComma() {
        String output = parsingService.parseLog("INFO,\"database, pool\"   ", ParserType.CSV);

        assertEquals("[\"INFO\",\"database, pool\"]", output);
    }

    @Test
    void rejectsMalformedCsvLog() {
        assertThrows(IllegalArgumentException.class,
                () -> parsingService.parseLog("INFO,\"unterminated", ParserType.CSV));
    }
}
