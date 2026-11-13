package com.tenextakehome.dto;

import com.tenextakehome.model.ParserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ParserDefinitionRequest {

    @NotBlank(message = "Parser name is required")
    private String parserName;

    @NotNull(message = "Parser type is required")
    private ParserType parserType;

    public ParserDefinitionRequest() {
    }

    public ParserDefinitionRequest(String parserName, ParserType parserType) {
        this.parserName = parserName;
        this.parserType = parserType;
    }

    public String getParserName() {
        return parserName;
    }

    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public ParserType getParserType() {
        return parserType;
    }

    public void setParserType(ParserType parserType) {
        this.parserType = parserType;
    }
}
