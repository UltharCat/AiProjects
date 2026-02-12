package com.knowledge.agent.rag.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
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
