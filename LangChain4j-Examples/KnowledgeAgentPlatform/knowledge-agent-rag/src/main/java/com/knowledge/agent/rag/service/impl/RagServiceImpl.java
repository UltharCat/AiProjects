package com.knowledge.agent.rag.service.impl;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class RagServiceImpl implements RagService {

    @Override
    public Result<Boolean> storeKnowledge(KnowledgeDTO knowledgeDTO) {
        return null;
    }

    @Override
    public Result<List<String>> searchKnowledge(String query) {
        return null;
    }

}
