package com.ai.agent.controller;

import com.ai.agent.request.AgentChatRequest;
import com.ai.agent.service.AgentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

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
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Flux<String> chat(@RequestBody @Valid AgentChatRequest request) throws IOException {
        return agentService.chat(request);
    }

}
