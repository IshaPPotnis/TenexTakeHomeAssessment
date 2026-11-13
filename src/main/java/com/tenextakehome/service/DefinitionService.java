package com.tenextakehome.service;

import com.tenextakehome.exception.ResourceNotFoundException;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.repository.ParserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefinitionService {

    private final ParserRepository parserRepository;

    public DefinitionService(ParserRepository parserRepository) {
        this.parserRepository = parserRepository;
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

    public ParserDefinition createDefinition(ParserDefinition definition) {
        if (parserRepository.existsById(definition.getParserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Parser definition already exists with id: " + definition.getParserId());
        }
        return parserRepository.save(definition);
    }

    public ParserDefinition updateDefinition(Long parserId, ParserDefinition definition) {
        if (!parserId.equals(definition.getParserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parser ID in the request body must match the path ID");
        }
        if (!parserRepository.existsById(parserId)) {
            throw new ResourceNotFoundException("Parser definition not found with id: " + parserId);
        }
        return parserRepository.save(definition);
    }

    public void deleteDefinition(Long parserId) {
        if (!parserRepository.existsById(parserId)) {
            throw new ResourceNotFoundException("Parser definition not found with id: " + parserId);
        }
        parserRepository.deleteById(parserId);
    }
}
