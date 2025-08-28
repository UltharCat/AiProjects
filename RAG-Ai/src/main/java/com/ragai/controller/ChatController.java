package com.ragai.controller;

import com.ragai.entity.Country;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(@Qualifier("chatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 同步接口响应对话
     * @param prompt
     * @return
     */
    @GetMapping("/singleChat")
    public String chat(@RequestParam("prompt") String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }

    /**
     * 同步响应实体返回对话
     */
    @GetMapping(value = "/singleChat/entity")
    public Country chatEntity(@RequestParam("prompt") String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(Country.class);
    }

    /**
     * SSE流式响应对话
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
     * Flux-SSE流式响应对话
     * @param prompt
     * @param response
     * @return
     */
    @GetMapping(value = "/stream/sse/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> charStreamFlux(@RequestParam("prompt") String prompt, HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        return chatClient.prompt()
                .user(prompt)
                .stream().content();
    }

    /**
     * streamable-HTTP流式响应对话
     * @param prompt
     * @return
     */
    @GetMapping(value = "/stream/http", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<StreamingResponseBody> chatStreamHttp(@RequestParam("prompt") String prompt) {
        StreamingResponseBody stream = output -> {
            chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content()
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(chunk -> {
                        try {
                            output.write(chunk.getBytes(StandardCharsets.UTF_8));
                            output.flush(); // 及时把分块推给客户端
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doFinally(sig -> {
                        try { output.close(); } catch (IOException ignored) {}
                    })
                    .blockLast(); // 在专用写线程中等待完成
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .body(stream);
    }

}
