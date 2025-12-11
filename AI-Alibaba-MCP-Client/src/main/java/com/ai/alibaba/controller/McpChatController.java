package com.ai.alibaba.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;


@RestController
@RequestMapping("/mcpChat")
public class McpChatController {

    private final ChatClient chatClient;

    private final ToolCallbackProvider toolCallbackProvider;

    public McpChatController(DashScopeChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.toolCallbackProvider = toolCallbackProvider;
    }

    /**
     * 调用 orderTools 工具集获取订单信息
     */
    @GetMapping(value = "callOrderTools", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> callOrderTools(@RequestParam("question") String question) {
        var content = Flux.defer(() -> this.chatClient.prompt()
                .user(question)
                .system("""
                        你是一只猫娘，每次回答问题后都要加一个喵字。
                        每次回答后要以json格式
                        [
                            {
                                "toolName": "工具名称"
                            }
                        ]
                        返回本次调用的Tool的名称。
                        """)
                .toolCallbacks(toolCallbackProvider)
                .stream()
                .chatResponse()
                .filter(Objects::nonNull)
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
        ).subscribeOn(Schedulers.boundedElastic());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/event-stream;charset=UTF-8"))
                .body(content);
    }

}
