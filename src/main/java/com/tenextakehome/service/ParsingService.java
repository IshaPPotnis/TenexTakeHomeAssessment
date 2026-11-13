package com.tenextakehome.service;

import com.tenextakehome.model.ParserType;
import com.tenextakehome.service.parser.Parser;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ParsingService {

    private final Map<ParserType, Parser> parsersByType = new EnumMap<>(ParserType.class);

    public ParsingService(List<Parser> parsers) {
        for (Parser parser : parsers) {
            Parser existing = parsersByType.put(parser.getParserType(), parser);
            if (existing != null) {
                throw new IllegalStateException("Multiple parsers configured for type " + parser.getParserType());
            }
        }
    }

    public String parseLog(String log, ParserType parserType) {
        if (parserType == null) {
            throw new IllegalArgumentException("Parser type must not be null");
        }

        Parser parser = parsersByType.get(parserType);
        if (parser == null) {
            throw new IllegalStateException("No parser configured for type " + parserType);
        }
        return parser.parse(log);
    }
}
