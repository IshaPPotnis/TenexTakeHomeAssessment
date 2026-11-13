package com.tenextakehome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.service.parser.CsvParser;
import com.tenextakehome.service.parser.JsonParser;
import com.tenextakehome.service.parser.XmlParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        Map<String, Object> output = parsingService.parseLog(
                "<log><level>INFO</level><message>started</message></log>", ParserType.XML);

        assertEquals(Map.of("log", Map.of("level", "INFO", "message", "started")), output);
    }

    @Test
    void parsesEventXmlWithItsChildrenAsRootKeys() {
        Map<String, Object> output = parsingService.parseLog(
                "<Event><EventID>4625</EventID><Channel>Security</Channel></Event>", ParserType.XML);

        assertEquals(Map.of("EventID", "4625", "Channel", "Security"), output);
    }

    @Test
    void parsesAndNormalizesJsonLog() {
        Map<String, Object> output = parsingService.parseLog("{ \"level\" : \"INFO\" }", ParserType.JSON);

        assertEquals(Map.of("level", "INFO"), output);
    }

    @Test
    void removesConfiguredFieldsFromParsedJsonObjectsRecursively() {
        Map<String, Object> output = parsingService.parseLog(
                "{\"level\":\"INFO\",\"metadata\":{\"token\":\"secret\",\"host\":\"app\"},"
                        + "\"events\":[{\"token\":\"nested-secret\",\"message\":\"started\"}]}",
                ParserType.JSON,
                java.util.List.of("token"));

        assertEquals(Map.of(
                "level", "INFO",
                "metadata", Map.of("host", "app"),
                "events", List.of(Map.of("message", "started"))), output);
    }

    @Test
    void removesConfiguredFieldsFromConvertedXml() {
        Map<String, Object> output = parsingService.parseLog(
                "<log><level>INFO</level><token>secret</token></log>",
                ParserType.XML,
                java.util.List.of("token"));

        assertEquals(Map.of("log", Map.of("level", "INFO")), output);
    }

    @Test
    void leavesParsedOutputUnchangedWhenNoFieldsAreConfiguredForRemoval() {
        Map<String, Object> output = parsingService.parseLog(
                "{\"level\":\"INFO\",\"message\":\"started\"}", ParserType.JSON, java.util.List.of());

        assertEquals(Map.of("level", "INFO", "message", "started"), output);
    }

    @Test
    void parsesCsvLogWithQuotedComma() {
        Map<String, Object> output = parsingService.parseLog("INFO,\"database, pool\"   ", ParserType.CSV);

        assertEquals(Map.of("values", List.of("INFO", "database, pool")), output);
    }

    @Test
    void parsesCsvLogUsingConfiguredColumnsAsObjectKeys() {
        Map<String, Object> output = parsingService.parseLog(
                "4625,Security,\"Failed, repeated login\"",
                ParserType.CSV,
                java.util.List.of(),
                java.util.List.of("EventID", "Channel", "Message"));

        assertEquals(Map.of(
                "EventID", "4625",
                "Channel", "Security",
                "Message", "Failed, repeated login"), output);
    }

    @Test
    void rejectsCsvLogWhenConfiguredColumnsDoNotMatchFieldCount() {
        assertThrows(IllegalArgumentException.class,
                () -> parsingService.parseLog(
                        "INFO,started",
                        ParserType.CSV,
                        java.util.List.of(),
                        java.util.List.of("level")));
    }

    @Test
    void rejectsMalformedCsvLog() {
        assertThrows(IllegalArgumentException.class,
                () -> parsingService.parseLog("INFO,\"unterminated", ParserType.CSV));
    }
}
