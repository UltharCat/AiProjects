package com.ai.alibaba.controller;

import com.ai.alibaba.config.model.AiModelFactory;
import com.ai.alibaba.entity.Country;
import com.ai.alibaba.service.VectorStoreService;
import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Model;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final AiModelFactory aiModelFactory;

    private Model<?, ?> aiModel;

    private final ConfigurablePromptTemplateFactory promptTemplateFactory;

    private final VectorStoreService vectorStoreService;

    public ChatController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory,
                          ConfigurablePromptTemplateFactory promptTemplateFactory,
                          VectorStoreService vectorStoreService) {
        this.aiModelFactory = aiModelFactory;
        this.aiModel = aiModelFactory.getModel(null);
        this.promptTemplateFactory = promptTemplateFactory;
        this.vectorStoreService = vectorStoreService;
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
        String instruction = "请仅返回严格的 JSON，不要包含任何解释或额外文本。";
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
                return new ObjectMapper().readValue(text, Country.class);
            } catch (Exception ex) {
                throw new RuntimeException("实体类转换失败: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * SSE流式响应对话（MVC编程）
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
        ((DashScopeChatModel) aiModel)
                .stream(prompt)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                // 对应函数式接口方法签名实现（即具备和函数式接口方法相同的入参和返回），同理emitter::completeWithError并不需要继承或实现Consumer，只需要相同的方法签名即可
                chunk -> {
                    try {
                        emitter.send(chunk);
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
     * Flux-SSE流式响应对话（WebFlux冷流响应式编程）
     * @param input 用户输入
     * @param response HttpServletResponse
     * @return Flux<String>
     */
    @GetMapping(value = "/stream/sse/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamFlux(@RequestParam("input") String input, HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        // 外部构造prompt避免匿名实现中重复构造
        DashScopeChatOptions options = DashScopeChatOptions.builder().build();
        Prompt prompt = new Prompt(input, options);
        // 通过Flux.defer延迟执行，无defer的情况下会在程序初始化时就创建flux发布者，导致后续的flux订阅始终面向一个发布者，defer的作用有两点：1.延迟加载 2.每次请求订阅时创建新的发布者
        return Flux.defer(() -> ((DashScopeChatModel) aiModel)
                // 获取flux流式响应（Flux的stream是动态推送处置的，对比的collection的stream则是静态的集合数据遍历处理）
                .stream(prompt)
                //.publishOn(Schedulers.boundedElastic())
                // 处理流式响应内容，提取文本
                .map(chatResponse -> {
                        if (chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                            return chatResponse.getResult().getOutput().getText();
                        } else {
                            return "";
                        }
                    }
                )
        )
        // 将上游处理切换到弹性线程池中异步执行
        // 包括defer延迟执行和后续supplier发布者的创建，如果subscribeOn在supplier实现内部，则只是将supplier的实现放在了弹性线程池中，不过区别不大，且如果在内部，也会有stream()可能已有线程，无法切换的问题
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * streamable-http流式对话
     * @param input 用户输入
     * @return StreamingResponseBody
     */
    @GetMapping(value = "/stream/http", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<StreamingResponseBody> chatStreamHttpV2(@RequestParam("input") String input) {
        DashScopeChatOptions options = DashScopeChatOptions.builder().build();
        Prompt prompt = new Prompt(input, options);

        StreamingResponseBody stream = outputStream -> ((DashScopeChatModel) aiModel)
                        .stream(prompt)
                        // 下游有阻塞 I/O、重 CPU 或复杂处理，使用publishOn将下游处理doOnNext和doFinally的处理切换到弹性线程池中异步执行
                        .publishOn(Schedulers.boundedElastic())
                        .doOnNext(chunk -> {
                            try {
                                byte[] bytes = chunk.getResult().getOutput().getText().getBytes(StandardCharsets.UTF_8);
                                // I/O阻塞操作
                                outputStream.write(bytes);
                                outputStream.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .doFinally(sig -> {
                            try {
                                // I/O阻塞操作
                                outputStream.close();
                            } catch (IOException ignored) {}
                        })
                        .blockLast();
        // ResponseEntity可以设置更多响应头，使流式返回格式限定为UTF-8
        // 该return其实是该流式传输的蓝图，规范了返回的通道是stream和格式，不过存在问题：一旦成功后就会确定http状态，后续消息传输无法更改这个状态
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
     * 通过模板配置工厂基于配置化模板快捷构造userMessage的对话
     * @param input
     * @return
     */
    @GetMapping("/promptTemplateFactoryChat")
    public AssistantMessage promptTemplateFactoryChat(@RequestParam(value = "input", defaultValue = "温州") String input) {
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate("test-template");
        if (template == null) {
            template = promptTemplateFactory.create("test-template", "你是一个天气预报员，用户询问你{city}天气，请回答用户明天的天气情况");
//            template = promptTemplateFactory.create("test-template", "你是一个天气预报员，用户询问你{city}天气，请回答用户明天的天气情况", Map.of("city", input));
        }
        Prompt prompt = template.create(Map.of("city", input));

        return ((DashScopeChatModel) aiModel).call(prompt).getResult().getOutput();
    }

    /**
     * 通过模板配置文件快捷构造userMessage的对话
     * @param input
     * @return
     */
    @GetMapping("/promptTemplateChat")
    public AssistantMessage promptTemplateChat(@RequestParam(value = "input", defaultValue = "温州") String input) {
        Prompt prompt = new PromptTemplate(
                    ResourceUtils.getText("classpath:prompts/user.st")
                )
                .create(Map.of("city", input));

//        Prompt prompt = new PromptTemplate(
//                ResourceUtils.getText("classpath:prompts/user.st")
//                , Map.of("city", input)
//        ).create();

        return ((DashScopeChatModel) aiModel).call(prompt).getResult().getOutput();
    }

    /**
     * 模板构造systemMessage的对话
     * @param input
     * @return
     */
    @GetMapping("/sysPromptTemplateChat")
    public AssistantMessage sysPromptTemplateChat(@RequestParam(value = "input", defaultValue = "温州") String input) {
        Message systemMessage = new SystemPromptTemplate(
                ResourceUtils.getText("classpath:prompts/system.st")
        ).createMessage();

        Message userMessage = new PromptTemplate(
                ResourceUtils.getText("classpath:prompts/user.st")
        ).createMessage(Map.of("city", input));

        Prompt prompt = new Prompt(Arrays.asList(systemMessage, userMessage));
        return ((DashScopeChatModel) aiModel).call(prompt).getResult().getOutput();
    }

    /**
     * 静态RAG聊天示例
     * @param question
     * @return
     */
    @GetMapping(value = "/staticRagChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> staticRagChat(@RequestParam(value = "question", defaultValue = "请告知文件概要信息") String question) {
        Message systemMessage = new SystemPromptTemplate(ResourceUtils.getText("classpath:prompts/policy/system.st")).createMessage();
        Message userMessage = new SystemPromptTemplate(ResourceUtils.getText("classpath:prompts/policy/user.st"))
                .createMessage(
                        Map.of("context", ResourceUtils.getText("classpath:docs/md/中共中央关于制定国民经济和社会发展第十五个五年规划的建议_中央有关文件_中国政府网/中共中央关于制定国民经济和社会发展第十五个五年规划的建议_中央有关文件_中国政府网.md"),
                                "question", question
                ));
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                // temperature越低越倾向RAG，多样性低
                .withTemperature(0.5)
                // TopP选择token采样范围，越大生成内容的采样范围越大，随机性越大
                .withTopP(0.9)
                .build();
        Prompt prompt = new Prompt(Arrays.asList(systemMessage, userMessage), options);

        var flux =  Flux.defer(() -> ((DashScopeChatModel) aiModel)
                .stream(prompt)
                .filter(chatResponse -> chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null)
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
        ).subscribeOn(Schedulers.boundedElastic());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/event-stream;charset=UTF-8"))
                .body(flux);
    }

    /**
     * 文件上传接口
     * @param file 文件
     * @param indexName 索引名称
     * @return 上传结果
     */
    @PostMapping("/rag/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "indexName", required = false, defaultValue = "local") String indexName) {
        if (vectorStoreService.saveFileToVectorStore(file, indexName)) {
            return ResponseEntity.ok().body("file uploaded to vector store");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("file upload to vector store failed");
    }

    /**
     * RAG聊天接口
     * @param question 用户问题
     * @return 回答内容
     */
    @GetMapping(value = "/rag/chat", produces =  MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> ragChat(@RequestParam("question") String question,
                                          @RequestParam(value = "indexName", required = false, defaultValue = "local") String indexName) {
        var produces = MediaType.parseMediaType("text/event-stream;charset=UTF-8");
        var retriever = vectorStoreService.createDocumentRetriever(indexName);
        // 检查检索结果
        List<Document> retrievedDocs = retriever.retrieve(Query.builder().text(question).build());

        if (CollectionUtils.isEmpty(retrievedDocs)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(produces)
                    .body(Flux.just("抱歉，我在知识库中没有找到与您问题相关的信息。"));
        }

        var chatClient = ChatClient.builder((ChatModel) aiModel)
                .defaultAdvisors(new DocumentRetrievalAdvisor(retriever))
                .build();

        var content = chatClient.prompt()
                .system(ResourceUtils.getText("classpath:prompts/policy/system.st"))
                .user(question)
                .stream()
                .content();

        return ResponseEntity.ok()
                .contentType(produces)
                .body(content);
    }


}
