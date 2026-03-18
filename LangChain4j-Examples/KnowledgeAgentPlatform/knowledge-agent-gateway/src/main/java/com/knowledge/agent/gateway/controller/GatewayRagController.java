package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class GatewayRagController {

    @DubboReference(check = false)
    private RagService ragService;

    @GetMapping("/search")
    public Result<List<KnowledgeDTO>> search(@RequestParam String query,
                                             @RequestParam(defaultValue = "3") Integer limit) {
        return ragService.searchKnowledge(GatewayUserContext.requireUserId(), query, limit);
    }
}
