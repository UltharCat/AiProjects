package com.ai.rag.repo;

import com.ai.rag.domain.TRagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RagDocumentRepository extends JpaRepository<TRagDocument, Long> {

}

