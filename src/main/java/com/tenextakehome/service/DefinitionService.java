package com.tenextakehome.service;

import com.tenextakehome.dto.ParserDefinitionRequest;
import com.tenextakehome.exception.ResourceNotFoundException;
import com.tenextakehome.model.CustomerScope;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.repository.ParserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefinitionService {

    private static final String PARSER_ID_SEQUENCE_KEY = "parser-definitions:sequence";

    private final ParserRepository parserRepository;
    private final StringRedisTemplate redisTemplate;

    public DefinitionService(ParserRepository parserRepository, StringRedisTemplate redisTemplate) {
        this.parserRepository = parserRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<ParserDefinition> getAllDefinitions() {
        List<ParserDefinition> definitions = new ArrayList<>();
        parserRepository.findAll().forEach(definitions::add);
        return definitions;
    }

    public List<ParserDefinition> getAllDefinitions(String customerName) {
        return getAllDefinitions().stream()
                .filter(definition -> CustomerScope.isVisibleTo(definition.getCustomerName(), customerName))
                .toList();
    }

    public ParserDefinition getDefinition(Long parserId) {
        return parserRepository.findById(parserId)
                .orElseThrow(() -> new ResourceNotFoundException("Parser definition not found with id: " + parserId));
    }

    public ParserDefinition getDefinition(Long parserId, String customerName) {
        ParserDefinition definition = getDefinition(parserId);
        if (!CustomerScope.isVisibleTo(definition.getCustomerName(), customerName)) {
            throw new ResourceNotFoundException("Parser definition not found with id: " + parserId);
        }
        return definition;
    }

    public ParserDefinition createDefinition(ParserDefinitionRequest request) {
        return createDefinition(request, CustomerScope.GLOBAL);
    }

    public ParserDefinition createGlobalDefinition(ParserDefinitionRequest request) {
        return createDefinition(request, CustomerScope.GLOBAL);
    }

    public void initializeGlobalColumnsIfMissing(Long parserId, List<String> columns) {
        ParserDefinition definition = getDefinition(parserId);
        if (!CustomerScope.isGlobal(definition.getCustomerName())) {
            throw new IllegalStateException("Cannot initialize columns for a customer parser definition");
        }
        if (definition.getColumns().isEmpty()) {
            definition.setColumns(columns);
            parserRepository.save(definition);
        }
    }

    public ParserDefinition createDefinition(ParserDefinitionRequest request, String customerName) {
        ParserDefinition definition = new ParserDefinition(
                request.getParserName(),
                nextParserId(),
                request.getParserType(),
                request.getFieldsToRemove(),
                request.getColumns());
        definition.setCustomerName(customerName);
        return parserRepository.save(definition);
    }

    public ParserDefinition updateDefinition(Long parserId, ParserDefinitionRequest request, String customerName) {
        ParserDefinition definition = getDefinition(parserId, customerName);
        if (CustomerScope.isGlobal(definition.getCustomerName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Global parser definitions cannot be modified");
        }
        definition.setParserName(request.getParserName());
        definition.setParserType(request.getParserType());
        definition.setFieldsToRemove(request.getFieldsToRemove());
        definition.setColumns(request.getColumns());
        return parserRepository.save(definition);
    }

    public void deleteDefinition(Long parserId, String customerName) {
        ParserDefinition definition = getDefinition(parserId, customerName);
        if (CustomerScope.isGlobal(definition.getCustomerName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Global parser definitions cannot be deleted");
        }
        parserRepository.deleteById(parserId);
    }

    private Long nextParserId() {
        long highestExistingId = 0;
        for (ParserDefinition definition : getAllDefinitions()) {
            highestExistingId = Math.max(highestExistingId, definition.getParserId());
        }

        redisTemplate.opsForValue().setIfAbsent(PARSER_ID_SEQUENCE_KEY, String.valueOf(highestExistingId));
        Long nextId = redisTemplate.opsForValue().increment(PARSER_ID_SEQUENCE_KEY);
        if (nextId == null) {
            throw new IllegalStateException("Unable to allocate parser ID from Redis");
        }
        return nextId;
    }
}
