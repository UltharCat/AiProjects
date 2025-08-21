package com.ragai.controller;

import com.ragai.entity.Country;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

//@RestController
@RequestMapping("/alibaba")
public class AlibabaController {

    private final ChatClient chatClient;

    public AlibabaController(@Qualifier("dashScopeChatClient") ChatClient dashScopeChatClient) {
        this.chatClient = dashScopeChatClient;
    }

    /**
     * 同步接口响应对话
     * @param prompt
     * @return
     */
    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }

    /**
     * MVC流式响应对话
     * @param prompt
     * @return
     */
    @GetMapping("/stream/sse")
    public SseEmitter charStreamSse(@RequestParam("prompt") String prompt, HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        SseEmitter emitter = new SseEmitter();
        chatClient.prompt().user(prompt).stream().content().subscribe(
                content -> {
                    try {
                        emitter.send(content);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    /**
     * Flux流式响应对话
     * @param prompt
     * @param response
     * @return
     */
    @GetMapping(value = "/stream/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> charStreamFlux(@RequestParam("prompt") String prompt, HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        return chatClient.prompt()
                .user(prompt)
                .stream().content();
    }

    /**
     * 同步响应实体返回对话
     */
    @GetMapping(value = "/chat/entity")
    public Country chatEntity(@RequestParam("prompt") String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(Country.class);
    }

}
