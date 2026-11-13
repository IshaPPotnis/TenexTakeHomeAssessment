package com.tenextakehome.service;

import com.tenextakehome.dto.ParserDefinitionRequest;
import com.tenextakehome.exception.ResourceNotFoundException;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.repository.ParserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    public ParserDefinition getDefinition(Long parserId) {
        return parserRepository.findById(parserId)
                .orElseThrow(() -> new ResourceNotFoundException("Parser definition not found with id: " + parserId));
    }

    public ParserDefinition createDefinition(ParserDefinitionRequest request) {
        ParserDefinition definition = new ParserDefinition(
                request.getParserName(),
                nextParserId(),
                request.getParserType(),
                request.getFieldsToRemove());
        return parserRepository.save(definition);
    }

    public ParserDefinition updateDefinition(Long parserId, ParserDefinitionRequest request) {
        ParserDefinition definition = getDefinition(parserId);
        definition.setParserName(request.getParserName());
        definition.setParserType(request.getParserType());
        definition.setFieldsToRemove(request.getFieldsToRemove());
        return parserRepository.save(definition);
    }

    public void deleteDefinition(Long parserId) {
        if (!parserRepository.existsById(parserId)) {
            throw new ResourceNotFoundException("Parser definition not found with id: " + parserId);
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
