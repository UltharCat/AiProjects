package com.ai.model.controller;

import com.ai.model.config.model.AiModelFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Model;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.model.config.constant.AiConstant.*;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final AiModelFactory aiModelFactory;

    private Model<?, ?> aiModel;

    private IntegrationType INTEGRATION_TYPE;

    public ChatController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory) {
        this.aiModelFactory = aiModelFactory;
        this.aiModel = aiModelFactory.getModel(null);
        this.INTEGRATION_TYPE = IntegrationType.OLLAMA;
    }

    /**
     * 切换模型
     * @param modelType 模型类型
     */
    @GetMapping("/changeModel")
    public void changeModel(@RequestParam(value = "modelType", required = false) String modelType) {
        this.aiModel = aiModelFactory.getModel(modelType);
        if (modelType.startsWith(IntegrationType.OPENAI.getType())) {
            INTEGRATION_TYPE = IntegrationType.OPENAI;
        } else {
            INTEGRATION_TYPE = IntegrationType.OLLAMA;
        }
    }

    /**
     * 同步接口响应对话
     * @param input 用户输入
     * @return 回复内容
     */
    @GetMapping("/singleChat")
    public String chat(@RequestParam("input") String input) {
        ChatResponse call;
        if (INTEGRATION_TYPE == IntegrationType.OPENAI) {
            OpenAiChatOptions options = OpenAiChatOptions.builder().build();
            Prompt prompt = new Prompt(input, options);
            call = ((OpenAiChatModel) aiModel).call(prompt);
        } else {
            OllamaOptions options = OllamaOptions.builder().build();
            Prompt prompt = new Prompt(input, options);
            call = ((OllamaChatModel) aiModel).call(prompt);
        }
        return call.getResult().getOutput().getText();
    }

//    /**
//     * SSE流式响应对话
//     * @param prompt 用户输入
//     * @param response HttpServletResponse
//     * @return SseEmitter
//     */
//    @GetMapping("/stream/sse")
//    public SseEmitter chatStreamSse(@RequestParam("prompt") String prompt, HttpServletResponse response) {
//        response.setContentType("text/event-stream;charset=UTF-8");
//        SseEmitter emitter = new SseEmitter();
//        chatClient.prompt().user(prompt).stream().content().subscribe(
//                content -> {
//                    try {
//                        emitter.send(content);
//                    } catch (Exception e) {
//                        emitter.completeWithError(e);
//                    }
//                },
//                emitter::completeWithError,
//                emitter::complete
//        );
//        return emitter;
//    }
//
//    /**
//     * Flux-SSE流式响应对话
//     * @param prompt 用户输入
//     * @param response HttpServletResponse
//     * @return Flux<String>
//     */
//    @GetMapping(value = "/stream/sse/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> chatStreamFlux(@RequestParam("prompt") String prompt, HttpServletResponse response) {
//        response.setContentType("text/event-stream;charset=UTF-8");
//        return chatClient.prompt()
//                .user(prompt)
//                .stream().content();
//    }
//
//    /**
//     * streamable-http流式对话
//     * @param prompt 用户输入
//     * @return StreamingResponseBody
//     */
//    @GetMapping(value = "/stream/http", produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseEntity<StreamingResponseBody> chatStreamHttpV2(@RequestParam("prompt") String prompt) {
//        StreamingResponseBody stream = output -> chatClient.prompt()
//                .user(prompt)
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
//
//    /**
//     * streamable-http流式对话V2（无乱码）
//     * @param prompt 用户输入
//     * @param httpServletRequest HttpServletRequest
//     * @return StreamingResponseBody
//     */
//    @GetMapping(value = "/memory/stream/http", produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseEntity<StreamingResponseBody> memoryStreamHttp(@RequestParam("prompt") String prompt, HttpServletRequest httpServletRequest) {
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
