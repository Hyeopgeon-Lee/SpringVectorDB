package kopo.poly.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kopo.poly.domain.Documents;
import kopo.poly.dto.DocumentsDTO;
import kopo.poly.repository.DocumentRepository;
import kopo.poly.service.IEmbeddingClient;
import kopo.poly.service.IEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Educational in-memory cosine-similarity search over documents in MongoDB.
 * This is NOT MongoDB Atlas Vector Search: all documents are scanned in memory.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService implements IEmbeddingService {

    private static final double SIMILARITY_THRESHOLD = 0.5;
    private static final int TOP_K = 10;

    private final IEmbeddingClient embeddingClient;
    private final DocumentRepository documentRepository;

    @Override
    public List<DocumentsDTO> findSimilarDocuments(DocumentsDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("검색 요청을 입력해 주세요.");
        }
        List<Double> query = generateEmbedding(request.question());

        return documentRepository.findAll().stream()
                .filter(document -> document.getEmbedding() != null)
                .map(document -> new ScoredDocument(
                        document, cosineSimilarity(query, document.getEmbedding())))
                .filter(scored -> scored.score() >= SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .limit(TOP_K)
                .map(scored -> DocumentsDTO.builder()
                        .id(scored.document().getId())
                        .content(scored.document().getContent())
                        .build())
                .toList();
    }

    @Override
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("임베딩할 텍스트를 입력해 주세요.");
        }

        Map<String, Object> response = embeddingClient.generateEmbedding(Map.of("text", text));
        if (response == null || !(response.get("embedding") instanceof List<?> raw) || raw.isEmpty()) {
            throw new IllegalStateException("Embedding API returned no vector");
        }

        // JSON numeric types can be Integer, Long, Float or Double.
        // Never use the unsafe (List<Double>) cast on untrusted HTTP data.
        List<Double> embedding = raw.stream().map(value -> {
            if (!(value instanceof Number number)) {
                throw new IllegalStateException("Embedding API returned a non-numeric vector");
            }
            double parsed = number.doubleValue();
            if (!Double.isFinite(parsed)) {
                throw new IllegalStateException("Embedding API returned a non-finite value");
            }
            return parsed;
        }).toList();

        if (embedding.stream().allMatch(value -> value == 0.0)) {
            throw new IllegalStateException("Embedding API returned a zero-length vector");
        }
        return embedding;
    }

    @Override
    public void saveDocument(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("저장할 문서를 입력해 주세요.");
        }
        List<Double> embedding = generateEmbedding(content);
        Documents document = Documents.builder()
                .content(content)
                .embedding(embedding)
                .build();

        documentRepository.save(document);
        log.info("Stored document with embedding dimension {}", embedding.size());
    }

    /**
     * Invalid or differently dimensioned old embeddings are skipped, rather than
     * aborting the whole query after an embedding model is changed.
     */
    private double cosineSimilarity(List<Double> first, List<Double> second) {
        if (first == null || second == null || first.isEmpty() || first.size() != second.size()) {
            return Double.NEGATIVE_INFINITY;
        }
        double dot = 0;
        double normFirst = 0;
        double normSecond = 0;
        for (int i = 0; i < first.size(); i++) {
            Double a = first.get(i);
            Double b = second.get(i);
            if (a == null || b == null || !Double.isFinite(a) || !Double.isFinite(b)) {
                return Double.NEGATIVE_INFINITY;
            }
            dot += a * b;
            normFirst += a * a;
            normSecond += b * b;
        }
        if (normFirst == 0 || normSecond == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        double score = dot / (Math.sqrt(normFirst) * Math.sqrt(normSecond));
        return Double.isFinite(score) ? score : Double.NEGATIVE_INFINITY;
    }

    private record ScoredDocument(Documents document, double score) {
        private ScoredDocument {
            Objects.requireNonNull(document);
        }
    }
}
