package com.tenextakehome.config;

import com.tenextakehome.dto.ParserDefinitionRequest;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.model.CustomerScope;
import com.tenextakehome.service.DefinitionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ParserDefinitionInitializer implements ApplicationRunner {

    private static final List<String> PREVIOUS_CSV_SAMPLE_COLUMNS = List.of("EventID", "Channel", "Message");
    private static final List<String> CSV_SAMPLE_COLUMNS = List.of(
            "@timestamp", "clientip", "remote_user", "request", "status",
            "body_bytes_sent", "request_time", "http_referrer", "http_user_agent", "EventID", "Channel");

    private final DefinitionService definitionService;

    public ParserDefinitionInitializer(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        var existingDefinitions = definitionService.getAllDefinitions().stream()
                .toList();
        Set<ParserType> existingSampleTypes = existingDefinitions.stream()
                .filter(definition -> CustomerScope.isGlobal(definition.getCustomerName())
                        && definition.getParserType() != null
                        && ("sample " + definition.getParserType().name().toLowerCase() + " parser")
                        .equals(definition.getParserName()))
                .map(ParserDefinition::getParserType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ParserType.class)));

        existingDefinitions.stream()
                .filter(definition -> CustomerScope.isGlobal(definition.getCustomerName())
                        && definition.getParserType() == ParserType.CSV
                        && "sample csv parser".equals(definition.getParserName()))
                .forEach(definition -> definitionService.updateGlobalColumnsIfDefault(
                        definition.getParserId(), PREVIOUS_CSV_SAMPLE_COLUMNS, CSV_SAMPLE_COLUMNS));

        for (ParserType parserType : ParserType.values()) {
            if (!existingSampleTypes.contains(parserType)) {
                String parserName = "sample " + parserType.name().toLowerCase() + " parser";
                List<String> columns = parserType == ParserType.CSV
                        ? CSV_SAMPLE_COLUMNS
                        : List.of();
                definitionService.createGlobalDefinition(
                        new ParserDefinitionRequest(parserName, parserType, List.of(), columns));
            }
        }
    }
}
