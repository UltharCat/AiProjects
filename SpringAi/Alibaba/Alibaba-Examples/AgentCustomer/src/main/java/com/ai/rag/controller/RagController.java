package com.ai.rag.controller;

import com.ai.rag.request.RagDocAddRequest;
import com.ai.rag.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 插入文本内容
     * @param request
     * @return
     */
    @PostMapping("/insert-content")
    public ResponseEntity<String> insertContent(@RequestBody @Validated(RagDocAddRequest.insertContent.class) RagDocAddRequest request) {
        boolean success = ragService.insertContent(request);
        return success ? ResponseEntity.ok("Content inserted successfully") : ResponseEntity.status(500).body("Failed to insert content");
    }

    /**
     * 上传文件
     * @param request
     * @return
     */
    @PostMapping(value = "/upload-file")
    public ResponseEntity<String> uploadFile(@RequestBody @Validated(RagDocAddRequest.uploadFile.class) RagDocAddRequest request) throws IOException {
        boolean success = ragService.uploadFile(request);
        return success ? ResponseEntity.ok("File uploaded successfully") : ResponseEntity.status(500).body("Failed to upload file");
    }

    /**
     * 搜索文档
     * @param content
     * @param topK
     * @return
     */
    @GetMapping("/search-documents")
    public ResponseEntity<?> searchDocuments(@RequestParam("content") String content, @RequestParam("topK") int topK) {
        var documents = ragService.searchDocuments(content, topK);
        return ResponseEntity.ok(documents);
    }

    /**
     * 删除文档
     * @param documentNumber
     * @return
     */
    @DeleteMapping("/delete-documents")
    public ResponseEntity<String> deleteDocuments(@RequestParam String documentNumber) {
        boolean success = ragService.deleteDocumentByNumber(documentNumber);
        return success ? ResponseEntity.ok("Documents deleted successfully") : ResponseEntity.status(500).body("Failed to delete documents");
    }

}
