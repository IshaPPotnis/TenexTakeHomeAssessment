package com.tenextakehome.dto;

import com.tenextakehome.model.ParserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ParserDefinitionRequest {

    @NotBlank(message = "Parser name is required")
    private String parserName;

    @NotNull(message = "Parser type is required")
    private ParserType parserType;

    private List<String> fieldsToRemove = new ArrayList<>();
    private List<String> columns = new ArrayList<>();

    public ParserDefinitionRequest() {
    }

    public ParserDefinitionRequest(String parserName, ParserType parserType) {
        this(parserName, parserType, List.of());
    }

    public ParserDefinitionRequest(String parserName, ParserType parserType, List<String> fieldsToRemove) {
        this(parserName, parserType, fieldsToRemove, List.of());
    }

    public ParserDefinitionRequest(
            String parserName,
            ParserType parserType,
            List<String> fieldsToRemove,
            List<String> columns) {
        this.parserName = parserName;
        this.parserType = parserType;
        setFieldsToRemove(fieldsToRemove);
        setColumns(columns);
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

    public List<String> getFieldsToRemove() {
        return fieldsToRemove;
    }

    public void setFieldsToRemove(List<String> fieldsToRemove) {
        this.fieldsToRemove = fieldsToRemove == null ? new ArrayList<>() : new ArrayList<>(fieldsToRemove);
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns == null ? new ArrayList<>() : new ArrayList<>(columns);
    }
}
