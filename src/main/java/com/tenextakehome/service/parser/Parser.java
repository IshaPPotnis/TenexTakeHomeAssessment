package com.tenextakehome.service.parser;

import com.tenextakehome.model.ParserType;

import java.util.List;

public interface Parser {

    ParserType getParserType();

    String parse(String input);

    default String parse(String input, List<String> columns) {
        return parse(input);
    }
}
