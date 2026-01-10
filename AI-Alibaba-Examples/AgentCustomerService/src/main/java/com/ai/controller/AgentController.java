package com.ai.controller;

import com.ai.request.AgentChatRequest;
import com.ai.service.AgentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 智能客服对话
     * @param request
     * @return
     */
    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody @Valid AgentChatRequest request) {
        return agentService.chat(request);
    }

}
