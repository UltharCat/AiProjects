package com.ai.alibaba.controller;

import com.ai.alibaba.config.model.AiModelFactory;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Model;
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
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final AiModelFactory aiModelFactory;

    private Model<?, ?> aiModel;

    public ChatController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory) {
        this.aiModelFactory = aiModelFactory;
        this.aiModel = aiModelFactory.getModel(null);
    }

    /**
     * 切换模型
     * @param modelType 模型类型
     */
    @GetMapping("/changeModel")
    public void changeModel(@RequestParam("modelType") String modelType) {
        this.aiModel = aiModelFactory.getModel(modelType);
    }

    /**
     * 同步接口响应对话
     * @param input 用户输入
     * @return 回复内容
     */
    @GetMapping("/singleChat")
    public String chat(@RequestParam("input") String input) {
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withMaxToken(1500)
                .build();
        Prompt prompt = new Prompt(input, options);
        ChatResponse call = ((DashScopeChatModel) aiModel).call(prompt);
        return call.getResult().getOutput().getText();
    }

    /**
     * SSE流式响应对话
     * @param input 用户输入
     * @param response HttpServletResponse
     * @return SseEmitter
     */
    @GetMapping("/stream/sse")
    public SseEmitter chatStreamSse(@RequestParam("input") String input, HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        SseEmitter emitter = new SseEmitter();
        DashScopeChatOptions options = DashScopeChatOptions.builder().build();
        Prompt prompt = new Prompt(input, options);
        ChatResponse call = ((DashScopeChatModel) aiModel).call(prompt);
        String text = call.getResult().getOutput().getText();
        try {
            emitter.send(text);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * Flux-SSE流式响应对话
     * @param input 用户输入
     * @param response HttpServletResponse
     * @return Flux<String>
     */
    @GetMapping(value = "/stream/sse/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamFlux(@RequestParam("input") String input, HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        DashScopeChatOptions options = DashScopeChatOptions.builder().build();
        Prompt prompt = new Prompt(input, options);
        return Flux.defer(() -> {
            ChatResponse call = ((DashScopeChatModel) aiModel).call(prompt);
            String text = call.getResult().getOutput().getText();
            // 如需分片流式返回，可将 text 拆分然后返回 Flux.fromArray(...)
            return Flux.just(text);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * streamable-http流式对话
     * @param input 用户输入
     * @return StreamingResponseBody
     */
    @GetMapping(value = "/stream/http", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<StreamingResponseBody> chatStreamHttpV2(@RequestParam("input") String input) {
        StreamingResponseBody stream = output -> {
            DashScopeChatOptions options = DashScopeChatOptions.builder().build();
            Prompt prompt = new Prompt(input, options);
            ChatResponse call = ((DashScopeChatModel) aiModel).call(prompt);
            String text = call.getResult().getOutput().getText();
            try (output) {
                try {
                    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                    output.write(bytes);
                    output.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException ignored) {
            }
        };
        // ResponseEntity可以设置更多响应头，使流式返回格式限定为UTF-8
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .body(stream);
    }

//    /**
//     * streamable-http流式对话
//     * @param input 用户输入
//     * @param httpServletRequest HttpServletRequest
//     * @return StreamingResponseBody
//     */
//    @GetMapping(value = "/memory/stream/http", produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseEntity<StreamingResponseBody> memoryStreamHttp(@RequestParam("input") String input, HttpServletRequest httpServletRequest) {
//        String sessionId = httpServletRequest.getSession().getId();
//        StreamingResponseBody stream = output -> chatClient.prompt()
//                .user(prompt)
//                .system(sp->
//                        sp.params(Map.of(
//                        "language", "日语"))
//                )
//                .advisors(a->
//                        a.params(Map.of(
//                                CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId,
//                                CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100
//                        ))
//                )
//                .stream()
//                .content()
//                .publishOn(Schedulers.boundedElastic())
//                .doOnNext(chunk -> {
//                    try {
//                        output.write(chunk.getBytes(StandardCharsets.UTF_8));
//                        output.flush();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .doFinally(sig -> {
//                    try { output.close(); } catch (IOException ignored) {}
//                })
//                .blockLast();
//        // ResponseEntity可以设置更多响应头，使流式返回格式限定为UTF-8
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
//                .body(stream);
//    }

}
