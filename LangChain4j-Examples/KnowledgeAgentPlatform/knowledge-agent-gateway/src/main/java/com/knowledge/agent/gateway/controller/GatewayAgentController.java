package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.service.AgentService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import com.knowledge.agent.gateway.model.GatewayChatRequest;
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
    public Result<String> chat(@RequestBody GatewayChatRequest request) {
        return agentService.chat(GatewayUserContext.requireUserId(), request.prompt());
    }

    @GetMapping("/chat/stream")
    public SseEmitter stream(@RequestParam String prompt) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        Result<String> result = agentService.chat(GatewayUserContext.requireUserId(), prompt);
        emitter.send(SseEmitter.event().name("message").data(result));
        emitter.complete();
        return emitter;
    }

    @PostMapping("/state")
    public Result<Void> switchState(@RequestParam String targetState) {
        return agentService.switchState(GatewayUserContext.requireUserId(), targetState);
    }
}
