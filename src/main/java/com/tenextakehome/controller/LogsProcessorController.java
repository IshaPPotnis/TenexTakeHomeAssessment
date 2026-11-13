package com.tenextakehome.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.dto.ProcessLogsRequest;
import com.tenextakehome.dto.ProcessLogsResponse;
import com.tenextakehome.dto.Result;
import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.model.DetectionRuleType;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.exception.LogProcessingException;
import com.tenextakehome.service.DetectionRuleService;
import com.tenextakehome.service.DefinitionService;
import com.tenextakehome.service.ParsingService;
import com.tenextakehome.service.RuleEvaluator;
import com.tenextakehome.service.AuthTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Logs", description = "Operations related to log processing")
public class LogsProcessorController {

    private final DefinitionService definitionService;
    private final ParsingService parsingService;
    private final DetectionRuleService detectionRuleService;
    private final ObjectMapper objectMapper;

    public LogsProcessorController(
            DefinitionService definitionService,
            ParsingService parsingService,
            DetectionRuleService detectionRuleService,
            ObjectMapper objectMapper) {
        this.definitionService = definitionService;
        this.parsingService = parsingService;
        this.detectionRuleService = detectionRuleService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/process")
    @Operation(summary = "Process log entries")
    public ResponseEntity<ProcessLogsResponse> processLogs(
            @Valid @RequestBody ProcessLogsRequest request,
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        ParserDefinition parser = definitionService.getDefinition(request.getParserId(), customerName);
        List<DetectionRule> detectionRules = detectionRuleService.getAllRules(customerName);
        List<Result> results = request.getLogs().stream()
                .map(log -> parseLog(log, parser))
                .collect(Collectors.toList());
        applyDetectionRules(results, detectionRules);
        return ResponseEntity.ok(new ProcessLogsResponse(results));
    }

    private void applyDetectionRules(List<Result> results, List<DetectionRule> detectionRules) {
        List<JsonNode> parsedLogs = results.stream()
                .map(result -> objectMapper.<JsonNode>valueToTree(result.getParsedOutput()))
                .toList();

        for (DetectionRule rule : detectionRules) {
            Predicate<JsonNode> firstCondition = compileRule(rule, rule.getRuleLogic());
            if (rule.getType() == DetectionRuleType.FOLLOWED_BY) {
                Predicate<JsonNode> followedByCondition = compileRule(rule, rule.getFollowedByRuleLogic());
                applyFollowedByRule(results, parsedLogs, rule, firstCondition, followedByCondition);
            } else {
                for (int i = 0; i < parsedLogs.size(); i++) {
                    if (firstCondition.test(parsedLogs.get(i))) {
                        results.get(i).getMatchedIncident().add(rule.getId() + "-" + rule.getName());
                    }
                }
            }
        }
    }

    private Predicate<JsonNode> compileRule(DetectionRule rule, String ruleLogic) {
        try {
            return RuleEvaluator.evaluate(ruleLogic);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid rule logic for detection rule " + rule.getId() + ": " + exception.getMessage(),
                    exception);
        }
    }

    private void applyFollowedByRule(
            List<Result> results,
            List<JsonNode> parsedLogs,
            DetectionRule rule,
            Predicate<JsonNode> firstCondition,
            Predicate<JsonNode> followedByCondition) {
        int firstEventCount = 0;
        int followedByEventCount = 0;
        int requiredOccurrences = rule.getRequiredOccurrences();
        int followedByRequiredOccurrences = rule.getFollowedByRequiredOccurrences();
        boolean firstConditionSatisfied = false;
        for (int i = 0; i < parsedLogs.size(); i++) {
            JsonNode parsedLog = parsedLogs.get(i);
            boolean sequenceWasReady = firstConditionSatisfied;
            if (sequenceWasReady && followedByCondition.test(parsedLog)) {
                followedByEventCount++;
                if (followedByEventCount >= followedByRequiredOccurrences) {
                    results.get(i).getMatchedIncident().add(rule.getId() + "-" + rule.getName());
                    firstEventCount = 0;
                    followedByEventCount = 0;
                    firstConditionSatisfied = false;
                }
            }
            if (!sequenceWasReady && firstCondition.test(parsedLog)) {
                firstEventCount++;
                if (firstEventCount >= requiredOccurrences) {
                    firstConditionSatisfied = true;
                }
            }
        }
    }

    @GetMapping(value = "/sample/{parserType}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get sample logs for a parser type")
    public ResponseEntity<String> getSampleLogs(@PathVariable ParserType parserType) {
        String resourceName = switch (parserType) {
            case XML -> "sampleXMLLog.txt";
            case JSON -> "sampleJsonLog.txt";
            case CSV -> "sampleCSVLog.txt";
        };
        try (var input = new ClassPathResource(resourceName).getInputStream()) {
            String sampleLogs = new String(
                    input.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(sampleLogs);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read sample log resource: " + resourceName, exception);
        }
    }

    private Result parseLog(String log, ParserDefinition parser) {
        try {
            Map<String, Object> parsedOutput = parsingService.parseLog(
                    log, parser.getParserType(), parser.getFieldsToRemove(), parser.getColumns());
            return new Result(log, parsedOutput,
                    "Parsed successfully using " + parser.getParserType() + ".");
        } catch (IllegalArgumentException exception) {
            throw new LogProcessingException(
                    "Log processing failed for " + parser.getParserType() + " input: " + exception.getMessage(),
                    exception);
        }
    }
}
