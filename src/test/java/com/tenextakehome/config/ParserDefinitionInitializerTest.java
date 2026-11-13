package com.tenextakehome.config;

import com.tenextakehome.dto.ParserDefinitionRequest;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.service.DefinitionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParserDefinitionInitializerTest {

    private final DefinitionService definitionService = mock(DefinitionService.class);
    private final ParserDefinitionInitializer initializer = new ParserDefinitionInitializer(definitionService);

    @Test
    void createsSampleDefinitionForEachMissingParserType() {
        when(definitionService.getAllDefinitions()).thenReturn(List.of());

        initializer.run(null);

        ArgumentCaptor<ParserDefinitionRequest> requests =
                ArgumentCaptor.forClass(ParserDefinitionRequest.class);
        verify(definitionService, times(ParserType.values().length)).createDefinition(requests.capture());
        for (ParserDefinitionRequest request : requests.getAllValues()) {
            org.junit.jupiter.api.Assertions.assertEquals(
                    "sample " + request.getParserType().name().toLowerCase() + " parser",
                    request.getParserName());
        }
    }

    @Test
    void doesNotCreateSampleDefinitionsThatAlreadyExist() {
        when(definitionService.getAllDefinitions()).thenReturn(List.of(
                new ParserDefinition("sample xml parser", 1L, ParserType.XML),
                new ParserDefinition("sample json parser", 2L, ParserType.JSON),
                new ParserDefinition("sample csv parser", 3L, ParserType.CSV)));

        initializer.run(null);

        verify(definitionService, never()).createDefinition(org.mockito.ArgumentMatchers.any());
    }
}
