package com.tenextakehome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.dto.ProcessLogsRequest;
import com.tenextakehome.dto.ProcessLogsResponse;
import com.tenextakehome.dto.Result;
import com.tenextakehome.exception.LogProcessingException;
import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.service.AuthTokenService;
import com.tenextakehome.service.DefinitionService;
import com.tenextakehome.service.DetectionRuleService;
import com.tenextakehome.service.ParsingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogsProcessorControllerTest {

    private final DefinitionService definitionService = mock(DefinitionService.class);
    private final ParsingService parsingService = mock(ParsingService.class);
    private final DetectionRuleService detectionRuleService = mock(DetectionRuleService.class);
    private final LogsProcessorController controller = new LogsProcessorController(
            definitionService, parsingService, detectionRuleService, new ObjectMapper());

    @Test
    void returnsSampleLogsFromClasspathResource() {
        ResponseEntity<String> response = controller.getSampleLogs(ParserType.XML);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("<EventID>4625</EventID>"));
    }

    @Test
    void includesEveryMatchingRuleAsIdAndName() {
        ParserDefinition parser = new ParserDefinition("JSON parser", 1L, ParserType.JSON);
        when(definitionService.getDefinition(1L, "admin-dev")).thenReturn(parser);
        when(parsingService.parseLog(
                "{\"EventID\":4625,\"Channel\":\"Security\"}", ParserType.JSON, List.of(), List.of()))
                .thenReturn("{\"EventID\":4625,\"Channel\":\"Security\"}");
        when(detectionRuleService.getAllRules("admin-dev")).thenReturn(List.of(
                new DetectionRule("Failed Login attempt", 1000L,
                        "EventID=4625 AND Channel=Security", "Failed login"),
                new DetectionRule("Security event", 1001L, "Channel=Security", "Security channel"),
                new DetectionRule("System event", 1002L, "Channel=System", "System channel")));

        ResponseEntity<ProcessLogsResponse> response = controller.processLogs(
                new ProcessLogsRequest(List.of("{\"EventID\":4625,\"Channel\":\"Security\"}"), 1L),
                "admin-dev");

        Result result = response.getBody().getResults().get(0);
        assertEquals(List.of("1000-Failed Login attempt", "1001-Security event"), result.getMatchedIncident());
    }

    @Test
    void throwsProcessingErrorWhenXmlParsingFails() {
        ParserDefinition parser = new ParserDefinition("XML parser", 1L, ParserType.XML);
        when(definitionService.getDefinition(1L, "admin-dev")).thenReturn(parser);
        when(parsingService.parseLog("not xml", ParserType.XML, List.of(), List.of()))
                .thenThrow(new IllegalArgumentException("Unable to parse XML log"));

        LogProcessingException exception = assertThrows(LogProcessingException.class,
                () -> controller.processLogs(new ProcessLogsRequest(List.of("not xml"), 1L), "admin-dev"));

        assertEquals("Log processing failed for XML input: Unable to parse XML log", exception.getMessage());
    }

    @Test
    void passesConfiguredCsvColumnsToParser() {
        List<String> columns = List.of("EventID", "Channel", "Message");
        ParserDefinition parser = new ParserDefinition(
                "CSV parser", 2L, ParserType.CSV, List.of(), columns);
        when(definitionService.getDefinition(2L, "admin-dev")).thenReturn(parser);
        when(parsingService.parseLog(
                "4625,Security,Failed login", ParserType.CSV, List.of(), columns))
                .thenReturn("{\"EventID\":\"4625\",\"Channel\":\"Security\",\"Message\":\"Failed login\"}");
        when(detectionRuleService.getAllRules("admin-dev")).thenReturn(List.of());

        controller.processLogs(
                new ProcessLogsRequest(List.of("4625,Security,Failed login"), 2L), "admin-dev");

        verify(parsingService).parseLog(
                "4625,Security,Failed login", ParserType.CSV, List.of(), columns);
    }
}
