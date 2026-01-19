package com.ai.agent.conrtoller;

import com.ai.agent.request.AgentChatRequest;
import com.ai.agent.response.AgentChatResponse;
import com.ai.agent.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService chatAgentService;

    public AgentController(@Qualifier("chatAgentService") AgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    /**
     * 与 Agent 进行对话交互的接口
     *
     * @param request 包含用户输入和上下文信息的请求对象
     * @return 包含 Agent 回复、日记正文和图片 URL 列表的响应对象
     */
    @PostMapping("/chat")
    public AgentChatResponse chat(@RequestBody @Valid AgentChatRequest request) {
        return chatAgentService.chat(request);
    }

}
