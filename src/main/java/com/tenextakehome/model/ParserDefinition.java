package com.tenextakehome.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("parser-definitions")
public class ParserDefinition {

    @NotBlank(message = "Parser name is required")
    private String parserName;

    @Id
    @NotNull(message = "Parser ID is required")
    private Long parserId;

    @NotNull(message = "Parser type is required")
    private ParserType parserType;

    public ParserDefinition() {
    }

    public ParserDefinition(String parserName, Long parserId, ParserType parserType) {
        this.parserName = parserName;
        this.parserId = parserId;
        this.parserType = parserType;
    }

    public String getParserName() {
        return parserName;
    }

    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public Long getParserId() {
        return parserId;
    }

    public void setParserId(Long parserId) {
        this.parserId = parserId;
    }

    public ParserType getParserType() {
        return parserType;
    }

    public void setParserType(ParserType parserType) {
        this.parserType = parserType;
    }
}
