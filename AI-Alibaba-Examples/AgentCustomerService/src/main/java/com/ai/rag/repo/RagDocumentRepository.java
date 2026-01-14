package com.ai.rag.repo;

import com.ai.rag.domain.TRagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RagDocumentRepository extends JpaRepository<TRagDocument, Long> {

    Optional<TRagDocument> findByDocumentNumber(String documentNumber);

    boolean deleteByDocumentNumber(String documentNumber);

}

