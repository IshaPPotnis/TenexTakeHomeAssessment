package com.tenextakehome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.dto.ProcessLogsRequest;
import com.tenextakehome.dto.ProcessLogsResponse;
import com.tenextakehome.dto.Result;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogsProcessorControllerTest {

    private final DefinitionService definitionService = mock(DefinitionService.class);
    private final ParsingService parsingService = mock(ParsingService.class);
    private final DetectionRuleService detectionRuleService = mock(DetectionRuleService.class);
    private final LogsProcessorController controller = new LogsProcessorController(
            definitionService, parsingService, detectionRuleService, new ObjectMapper());

    @Test
    void includesEveryMatchingRuleAsIdAndName() {
        ParserDefinition parser = new ParserDefinition("JSON parser", 1L, ParserType.JSON);
        when(definitionService.getDefinition(1L, "admin-dev")).thenReturn(parser);
        when(parsingService.parseLog("{\"EventID\":4625,\"Channel\":\"Security\"}", ParserType.JSON, List.of()))
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
    void returnsEmptyMatchedIncidentsWhenParsingFails() {
        ParserDefinition parser = new ParserDefinition("JSON parser", 1L, ParserType.JSON);
        when(definitionService.getDefinition(1L, "admin-dev")).thenReturn(parser);
        when(parsingService.parseLog("not json", ParserType.JSON, List.of()))
                .thenThrow(new IllegalArgumentException("Unable to parse JSON log"));
        when(detectionRuleService.getAllRules("admin-dev")).thenReturn(List.of(
                new DetectionRule("Any event", 1000L, "EventID=4625", "Any event")));

        ResponseEntity<ProcessLogsResponse> response = controller.processLogs(
                new ProcessLogsRequest(List.of("not json"), 1L), "admin-dev");

        Result result = response.getBody().getResults().get(0);
        assertEquals("Unable to parse JSON log", result.getNotes());
        assertEquals(List.of(), result.getMatchedIncident());
    }
}
