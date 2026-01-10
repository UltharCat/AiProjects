package com.ai.service.impl;

import com.ai.repo.RagDocumentRepository;
import com.ai.service.RagService;
import org.springframework.ai.document.Document;

import java.io.File;
import java.util.List;

public class RagServiceImpl implements RagService {

    private final RagDocumentRepository repo;

    public RagServiceImpl(RagDocumentRepository repo) {
        this.repo = repo;
    }


    @Override
    public boolean insertContent(String content) {
        return false;
    }

    @Override
    public boolean uploadFile(File file) {
        return false;
    }

    @Override
    public List<Document> searchDocuments(String query, int topK) {
        return List.of();
    }

}
