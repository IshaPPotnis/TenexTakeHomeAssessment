package com.tenextakehome.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenextakehome.model.ParserType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser implements Parser {

    private final ObjectMapper objectMapper;

    public CsvParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ParserType getParserType() {
        return ParserType.CSV;
    }

    @Override
    public String parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("CSV log must not be null or blank");
        }

        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean insideQuotes = false;
        boolean quoteClosed = false;

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (insideQuotes) {
                if (current == '"') {
                    if (i + 1 < input.length() && input.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        insideQuotes = false;
                        quoteClosed = true;
                    }
                } else {
                    field.append(current);
                }
            } else if (current == ',' && !quoteClosed) {
                fields.add(field.toString());
                field.setLength(0);
            } else if (current == '"' && field.isEmpty() && !quoteClosed) {
                insideQuotes = true;
            } else if (quoteClosed) {
                if (current == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                    quoteClosed = false;
                } else if (!Character.isWhitespace(current)) {
                    throw new IllegalArgumentException("Unexpected character after quoted CSV field");
                }
            } else if (current == '"') {
                throw new IllegalArgumentException("Unexpected quote in CSV field");
            } else {
                field.append(current);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException("Unterminated quoted CSV field");
        }
        fields.add(field.toString());

        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize parsed CSV log", exception);
        }
    }
}
