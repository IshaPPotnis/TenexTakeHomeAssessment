package com.tenextakehome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.service.parser.Parser;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParsingService {

    private final Map<ParserType, Parser> parsersByType = new EnumMap<>(ParserType.class);
    private final ObjectMapper objectMapper;

    public ParsingService(List<Parser> parsers, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        for (Parser parser : parsers) {
            Parser existing = parsersByType.put(parser.getParserType(), parser);
            if (existing != null) {
                throw new IllegalStateException("Multiple parsers configured for type " + parser.getParserType());
            }
        }
    }

    public Map<String, Object> parseLog(String log, ParserType parserType) {
        return parseLog(log, parserType, List.of());
    }

    public Map<String, Object> parseLog(String log, ParserType parserType, List<String> fieldsToRemove) {
        return parseLog(log, parserType, fieldsToRemove, List.of());
    }

    public Map<String, Object> parseLog(
            String log,
            ParserType parserType,
            List<String> fieldsToRemove,
            List<String> columns) {
        if (parserType == null) {
            throw new IllegalArgumentException("Parser type must not be null");
        }

        Parser parser = parsersByType.get(parserType);
        if (parser == null) {
            throw new IllegalStateException("No parser configured for type " + parserType);
        }
        try {
            JsonNode parsedJson = objectMapper.readTree(parser.parse(log, columns));
            if (fieldsToRemove != null && !fieldsToRemove.isEmpty()) {
                Set<String> excludedFields = fieldsToRemove.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(field -> !field.isEmpty())
                        .collect(Collectors.toSet());
                if (!excludedFields.isEmpty()) {
                    removeFields(parsedJson, excludedFields);
                }
            }
            return toMap(parsedJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to apply parser field exclusions", exception);
        }
    }

    private Map<String, Object> toMap(JsonNode parsedJson) {
        if (parsedJson.isObject()) {
            return objectMapper.convertValue(parsedJson, new TypeReference<>() { });
        }
        Map<String, Object> output = new LinkedHashMap<>();
        if (parsedJson.isArray()) {
            output.put("values", objectMapper.convertValue(parsedJson, new TypeReference<List<Object>>() { }));
        } else {
            output.put("value", objectMapper.convertValue(parsedJson, Object.class));
        }
        return output;
    }

    private void removeFields(JsonNode node, Set<String> fieldsToRemove) {
        if (node instanceof ObjectNode objectNode) {
            fieldsToRemove.forEach(objectNode::remove);
            objectNode.elements().forEachRemaining(child -> removeFields(child, fieldsToRemove));
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.elements().forEachRemaining(child -> removeFields(child, fieldsToRemove));
        }
    }
}
