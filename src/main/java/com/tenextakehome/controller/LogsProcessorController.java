package com.tenextakehome.controller;

import com.tenextakehome.dto.ProcessLogsRequest;
import com.tenextakehome.dto.ProcessLogsResponse;
import com.tenextakehome.dto.Result;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.service.DefinitionService;
import com.tenextakehome.service.ParsingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Logs", description = "Operations related to log processing")
public class LogsProcessorController {

    private final DefinitionService definitionService;
    private final ParsingService parsingService;

    public LogsProcessorController(DefinitionService definitionService, ParsingService parsingService) {
        this.definitionService = definitionService;
        this.parsingService = parsingService;
    }

    @PostMapping("/process")
    @Operation(summary = "Process log entries")
    public ResponseEntity<ProcessLogsResponse> processLogs(@Valid @RequestBody ProcessLogsRequest request) {
        ParserDefinition parser = definitionService.getDefinition(request.getParserId());
        List<Result> results = request.getLogs().stream()
                .map(log -> parseLog(log, parser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ProcessLogsResponse(results));
    }

    private Result parseLog(String log, ParserDefinition parser) {
        try {
            String parsedOutput = parsingService.parseLog(log, parser.getParserType());
            return new Result(log, parsedOutput, "Parsed successfully using " + parser.getParserType() + ".");
        } catch (IllegalArgumentException exception) {
            return new Result(log, null, exception.getMessage());
        }
    }
}
