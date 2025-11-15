package com.ai.alibaba.controller;

import com.ai.alibaba.config.model.AiModelFactory;
import com.ai.alibaba.entity.Country;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final AiModelFactory aiModelFactory;

    private Model<?, ?> aiModel;

    private final ConfigurablePromptTemplateFactory promptTemplateFactory;

    public ChatController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory,
                          ConfigurablePromptTemplateFactory promptTemplateFactory) {
        this.aiModelFactory = aiModelFactory;
        this.aiModel = aiModelFactory.getModel(null);
        this.promptTemplateFactory = promptTemplateFactory;
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
     * 同步响应结构化实体返回对话
     */
    @GetMapping(value = "/singleChat/entity")
    public Country chatEntity(@RequestParam("input") String input) {
        // 指示模型仅返回严格 JSON，字段按 Country 定义
        String instruction = "请仅返回严格的 JSON，不要包含任何解释或额外文本，JSON 字段与 Country 类一致。";
        String promptInput = instruction + "\n\n用户输入: " + input;

        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withMaxToken(1500)
                .build();
        Prompt prompt = new Prompt(promptInput, options);
        ChatResponse call = ((DashScopeChatModel) aiModel).call(prompt);
        String text = call.getResult().getOutput().getText();

        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<Country>() {});

        try {
            // 直接使用 converter 进行结构化转换
            return converter.convert(text);
        } catch (Exception e) {
            // 回退方案：使用 Jackson 解析严格 JSON
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().readValue(text, Country.class);
            } catch (Exception ex) {
                throw new RuntimeException("Country 转换失败: " + ex.getMessage(), ex);
            }
        }
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

    /**
     * 通过模板配置工厂基于配置化模板的对话
     * @param input
     * @return
     */
    @GetMapping("/promptTemplateFactoryChat")
    public AssistantMessage promptTemplateFactoryChat(@RequestParam(value = "input", defaultValue = "温州") String input) {
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate("test-template");
        if (template == null) {
            template = promptTemplateFactory.create("test-template", "你是一个天气预报员，用户询问你{city}天气，请回答用户明天的天气情况");
        }
        Prompt prompt = template.create(Map.of("city", input));

        return ((DashScopeChatModel) aiModel).call(prompt).getResult().getOutput();
    }

    /**
     * 通过模板配置文件的对话
     * @param input
     * @return
     */
    @GetMapping("/promptTemplateChat")
    public AssistantMessage promptTemplateChat(@RequestParam(value = "input", defaultValue = "温州") String input) {
        Prompt prompt = new PromptTemplate(
                    ResourceUtils.getText("classpath:prompts/weather-prompt.st")
                )
                .create(Map.of("city", input));

        return ((DashScopeChatModel) aiModel).call(prompt).getResult().getOutput();
    }

}
