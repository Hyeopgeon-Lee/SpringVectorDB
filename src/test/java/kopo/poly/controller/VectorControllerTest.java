package kopo.poly.controller;

import kopo.poly.dto.DocumentsDTO;
import kopo.poly.service.IEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VectorControllerTest {
    private IEmbeddingService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(IEmbeddingService.class);
        mvc = MockMvcBuilders.standaloneSetup(new VectorController(service)).build();
    }

    @Test
    void keepsExistingDocumentEndpoint() throws Exception {
        mvc.perform(post("/vector/v1/document").contentType("text/plain")
                        .content("Spring Boot 4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value(1));
        verify(service).saveDocument("Spring Boot 4");
    }

    @Test
    void keepsExistingSimilarityEndpoint() throws Exception {
        when(service.findSimilarDocuments(any())).thenReturn(
                List.of(DocumentsDTO.builder().id("one").content("match").build()));
        mvc.perform(post("/vector/v1/generate")
                        .contentType("text/plain").content("question"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("one"));
    }

    @Test
    void batchSavesDocuments() throws Exception {
        mvc.perform(post("/vector/v1/batch-documents")
                        .contentType("application/json")
                        .content("{\"contents\":[\"first\",\"second\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value(1));
        verify(service).saveDocument("first");
        verify(service).saveDocument("second");
    }

    @Test
    void rejectsBadBatchWithoutPartialWrites() throws Exception {
        mvc.perform(post("/vector/v1/batch-documents")
                        .contentType("application/json")
                        .content("{\"contents\":[\"valid\",\" \"]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
