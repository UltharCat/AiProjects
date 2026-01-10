package com.ai.repo;

import com.ai.domain.TRagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RagDocumentRepository extends JpaRepository<TRagDocument, Long> {

}

