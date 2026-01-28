package com.ai.agent.conrtoller;

import com.ai.agent.request.AgentChatRequest;
import com.ai.agent.response.AgentChatResponse;
import com.ai.agent.service.DiaryService;
import com.ai.agent.service.GuidService;
import jakarta.validation.Valid;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final DiaryService diaryService;
    private final GuidService guidService;

    public AgentController(DiaryService diaryService,
                           GuidService guidService) {
        this.diaryService = diaryService;
        this.guidService = guidService;
    }

    /**
     * 与 Agent 进行对话交互的接口
     *
     * @param request 包含用户输入和上下文信息的请求对象
     * @return 包含 Agent 回复、日记正文和图片 URL 列表的响应对象
     */
    @PostMapping("/chat")
    public AgentChatResponse chat(@RequestBody @Valid AgentChatRequest request) {
        if (!CollectionUtils.isEmpty(request.imgUrls())){
            // 图文模型解析图片，生成用户的心情日记，后续agent需要附加分析这篇心情日记
            return AgentChatResponse.builder()
                    .responseText("当前版本不支持图片处理，敬请期待！")
                    .build();
        }
        var memoryId = request.memoryId();
        var content = request.content();
        boolean isGreeting = guidService.isGreeting(content);
        if (isGreeting) {
            return AgentChatResponse.builder()
                    .responseText("你好！很高兴见到你！有什么我可以帮忙的吗？")
                    .build();
        } else {
            return diaryService.chat(memoryId, content);
        }
    }

}
