package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.common.resp.Result;

import java.util.List;

public interface RagService {

    /**
     * 存储知识到向量数据库和图谱
     * @param knowledgeDTO
     * @return
     */
    Result<Boolean> storeKnowledge(KnowledgeDTO knowledgeDTO);

    /**
     * 检索相关知识
     * @param query
     * @return
     */
    Result<List<String>> searchKnowledge(String query);

}
