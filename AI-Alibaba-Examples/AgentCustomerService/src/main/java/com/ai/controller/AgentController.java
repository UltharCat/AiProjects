package com.ai.controller;

import com.ai.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
     * @param question
     * @param conversationId
     * @return
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam("question") String question, @RequestParam("conversationId") String conversationId) {
        return agentService.chat(conversationId, question);
    }

}
