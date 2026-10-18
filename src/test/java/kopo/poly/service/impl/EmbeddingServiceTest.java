package kopo.poly.service.impl;

import java.util.List;
import java.util.Map;

import kopo.poly.domain.Documents;
import kopo.poly.dto.DocumentsDTO;
import kopo.poly.repository.DocumentRepository;
import kopo.poly.service.IEmbeddingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbeddingServiceTest {

    private IEmbeddingClient client;
    private DocumentRepository repository;
    private EmbeddingService service;

    @BeforeEach
    void setUp() {
        client = mock(IEmbeddingClient.class);
        repository = mock(DocumentRepository.class);
        service = new EmbeddingService(client, repository);
    }

    @Test
    void savesDocumentWithConvertedNumericEmbedding() {
        when(client.generateEmbedding(Map.of("text", "example")))
                .thenReturn(Map.of("embedding", List.of(1, 0.5, 0)));
        service.saveDocument("example");
        verify(repository).save(argThat(doc ->
                doc.getContent().equals("example")
                        && doc.getEmbedding().equals(List.of(1.0, 0.5, 0.0))));
    }

    @Test
    void similarityOrdersResultsAndSkipsInvalidVectors() {
        when(client.generateEmbedding(Map.of("text", "question")))
                .thenReturn(Map.of("embedding", List.of(1.0, 0.0)));
        when(repository.findAll()).thenReturn(List.of(
                Documents.builder().id("high").content("very relevant")
                        .embedding(List.of(1.0, 0.0)).build(),
                Documents.builder().id("low").content("less relevant")
                        .embedding(List.of(0.8, 0.6)).build(),
                Documents.builder().id("negative").content("irrelevant")
                        .embedding(List.of(-1.0, 0.0)).build(),
                Documents.builder().id("mismatch").content("wrong dimension")
                        .embedding(List.of(1.0, 1.0, 1.0)).build(),
                Documents.builder().id("zero").content("zero vector")
                        .embedding(List.of(0.0, 0.0)).build()
        ));
        var results = service.findSimilarDocuments(
                DocumentsDTO.builder().question("question").build());
        assertEquals(List.of("high", "low"),
                results.stream().map(DocumentsDTO::id).toList());
    }

    @Test
    void rejectsMissingEmbeddingOrInvalidNumericValue() {
        when(client.generateEmbedding(Map.of("text", "empty")))
                .thenReturn(Map.of("message", "no vector"));
        assertThrows(IllegalStateException.class, () -> service.generateEmbedding("empty"));

        when(client.generateEmbedding(Map.of("text", "bad")))
                .thenReturn(Map.of("embedding", List.of("not numeric")));
        assertThrows(IllegalStateException.class, () -> service.generateEmbedding("bad"));
    }

    @Test
    void rejectsBlankInputsWithoutCallingServices() {
        assertThrows(IllegalArgumentException.class, () -> service.saveDocument(" "));
        assertThrows(IllegalArgumentException.class, () -> service.generateEmbedding(""));
        verifyNoInteractions(client, repository);
    }
}
