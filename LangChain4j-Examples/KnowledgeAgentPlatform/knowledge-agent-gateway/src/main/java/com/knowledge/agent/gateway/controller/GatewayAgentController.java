package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.request.ChatRequest;
import com.knowledge.agent.api.service.AgentService;
import com.knowledge.agent.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/agent")
public class GatewayAgentController {

    @DubboReference(check = false)
    private AgentService agentService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        return agentService.chat(request.userId(), request.prompt());
    }

    @GetMapping("/chat/stream")
    public SseEmitter stream(@RequestParam Long userId, @RequestParam String prompt) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        Result<String> result = agentService.chat(userId, prompt);
        emitter.send(SseEmitter.event().name("message").data(result));
        emitter.complete();
        return emitter;
    }

    @PostMapping("/state")
    public Result<Void> switchState(@RequestParam Long userId, @RequestParam String targetState) {
        return agentService.switchState(userId, targetState);
    }
}
