package com.tenextakehome.service.parser;

import com.tenextakehome.model.ParserType;

public interface Parser {

    ParserType getParserType();

    String parse(String input);
}
