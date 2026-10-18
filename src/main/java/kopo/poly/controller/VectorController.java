package kopo.poly.controller;

import java.util.List;

import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.DocumentsDTO;
import kopo.poly.dto.MsgDTO;
import kopo.poly.service.IEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backwards compatible endpoints: POST /vector/v1/generate,
 * /vector/v1/document, and /vector/v1/batch-documents.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/vector/v1")
public class VectorController {

    private final IEmbeddingService embeddingService;

    @PostMapping("/generate")
    public ResponseEntity<CommonResponse<List<DocumentsDTO>>> generateAnswer(
            @RequestBody String question) {
        List<DocumentsDTO> matches = embeddingService.findSimilarDocuments(
                DocumentsDTO.builder().question(requireText(question)).build());
        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "OK", matches));
    }

    @PostMapping("/document")
    public ResponseEntity<CommonResponse<MsgDTO>> saveDocument(
            @RequestBody String content) {
        embeddingService.saveDocument(requireText(content));
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK,
                "OK", MsgDTO.builder().result(1).msg("저장되었습니다.").build()));
    }

    @PostMapping("/batch-documents")
    public ResponseEntity<CommonResponse<MsgDTO>> saveBatchDocuments(
            @RequestBody DocumentsDTO request) {
        if (request == null || request.contents() == null || request.contents().isEmpty()) {
            throw new IllegalArgumentException("저장할 문서 목록을 입력해 주세요.");
        }
        // Validate all inputs before writing any document.
        request.contents().forEach(this::requireText);
        request.contents().forEach(embeddingService::saveDocument);
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK,
                "OK", MsgDTO.builder().result(1).msg("여러 건이 저장되었습니다.").build()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<String>> handleInvalidInput(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(CommonResponse.of(HttpStatus.BAD_REQUEST, exception.getMessage(), null));
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("내용을 입력해 주세요.");
        }
        return value;
    }
}
