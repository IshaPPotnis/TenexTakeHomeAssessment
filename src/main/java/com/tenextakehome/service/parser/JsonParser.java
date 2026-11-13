package com.tenextakehome.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.model.ParserType;
import org.springframework.stereotype.Component;

@Component
public class JsonParser implements Parser {

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ParserType getParserType() {
        return ParserType.JSON;
    }

    @Override
    public String parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("JSON log must not be null or blank");
        }

        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(input));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to parse JSON log", exception);
        }
    }
}
