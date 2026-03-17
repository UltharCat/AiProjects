package com.knowledge.agent.core.tool;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class DubboAgentToolRouter implements AgentToolRouter {

    @DubboReference(check = false)
    private UserService userService;

    @DubboReference(check = false)
    private RagService ragService;

    @Override
    public String getUserProfile(Long userId) {
        Result<String> result = userService.getUserProfile(userId);
        return unwrap(result, "{}");
    }

    @Override
    public List<KnowledgeDTO> searchKnowledge(Long userId, String query, int limit) {
        Result<List<KnowledgeDTO>> result = ragService.searchKnowledge(userId, query, limit);
        return unwrap(result, List.of());
    }

    @Override
    public List<KnowledgeDTO> listPendingReviews(Long userId, int limit) {
        Result<List<KnowledgeDTO>> result = ragService.listPendingReviews(userId, limit);
        return unwrap(result, List.of());
    }

    @Override
    public boolean saveKnowledge(Long userId, String summary, Set<String> tags) {
        Result<Boolean> result = ragService.saveKnowledge(KnowledgeDTO.builder()
                .userId(userId)
                .summary(summary)
                .tags(tags)
                .build());
        return unwrap(result, Boolean.FALSE);
    }

    @Override
    public void updateReviewStatus(Long knowledgeId, int quality) {
        Result<Void> result = ragService.updateReviewStatus(knowledgeId, quality);
        if (result == null || result.getCode() != 200) {
            throw new BizException(result == null ? 500 : result.getCode(), result == null ? "review update failed" : result.getMessage());
        }
    }

    private <T> T unwrap(Result<T> result, T defaultValue) {
        if (result == null) {
            log.warn("Dubbo tool call returned null result");
            return defaultValue;
        }
        if (result.getCode() != 200) {
            throw new BizException(result.getCode(), result.getMessage());
        }
        return result.getData() == null ? defaultValue : result.getData();
    }
}
