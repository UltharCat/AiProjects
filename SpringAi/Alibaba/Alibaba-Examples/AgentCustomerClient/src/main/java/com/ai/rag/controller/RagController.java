package com.ai.rag.controller;

import com.ai.rag.dto.RagDocDto;
import com.ai.rag.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * @param content
     * @param documentNumber
     * @return
     */
    @GetMapping("/insert-content")
    public ResponseEntity<String> insertContent(@RequestParam("content") String content,
                                                @RequestParam(value = "documentNumber", required = false) String documentNumber) {
        boolean success = ragService.insertContent(RagDocDto.builder().content(content).documentNumber(documentNumber).build());
        return success ? ResponseEntity.ok("Content inserted successfully") : ResponseEntity.status(500).body("Failed to insert content");
    }

    /**
     * 上传文件
     * @param ragFile
     * @param documentNumber
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestPart("ragFile") MultipartFile ragFile,
                                             @RequestParam(value = "documentNumber", required = false) String documentNumber) throws IOException {
        boolean success = ragService.uploadFile(RagDocDto.builder().ragFile(ragFile).documentNumber(documentNumber).build());
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
