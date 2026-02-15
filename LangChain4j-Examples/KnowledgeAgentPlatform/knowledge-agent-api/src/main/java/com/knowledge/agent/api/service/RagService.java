package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.common.resp.Result;


/**
 * 知识存储与 RAG 核心服务
 * Dubbo 接口
 */
public interface RagService {

    /**
     * 保存知识点 (包含了 MySQL 存本体和 Milvus 存向量的双写逻辑)
     * @param dto 知识传输对象
     * @return 是否成功
     */
    Result<Boolean> saveKnowledge(KnowledgeDTO dto);

    /**
     * 更新艾宾浩斯复习状态 (不涉及向量更新，只更新 MySQL)
     * @param id 知识点ID
     * @param quality 回忆质量 0..5
     * @return void
     */
    Result<Void> updateReviewStatus(Long id, int quality);
}
