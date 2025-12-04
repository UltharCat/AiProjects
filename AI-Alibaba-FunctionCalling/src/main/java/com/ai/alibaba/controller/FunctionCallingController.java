package com.ai.alibaba.controller;

import com.ai.alibaba.dto.OrderDTO;
import com.ai.alibaba.service.OrderService;
import com.ai.alibaba.tools.OrderTools;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/functionCalling")
public class FunctionCallingController {

    private final ChatClient chatClient;

    private final OrderService orderService;

    private final OrderTools orderTools;

    public FunctionCallingController(DashScopeChatModel chatModel,
                                     OrderService orderService,
                                     OrderTools orderTools) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.orderService = orderService;
        this.orderTools = orderTools;
    }

    @GetMapping("findAllOrder")
    public ResponseEntity<List<OrderDTO>> findAllOrder() {
        return ResponseEntity.ok(orderService.findAllOrder().orElseGet(Collections::emptyList));
    }

    /**
     * 调用 orderFunction 函数获取所有订单信息
     */
    @GetMapping(value = "callOrderFunction", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> callOrderFunction(@RequestParam("question") String question) {
        var content = Flux.defer(() -> this.chatClient.prompt()
                .user(question)
                .system("""
                        你是一只猫娘，每次回答问题后都要加一个喵字。
                        """)
                .functions("orderFunction")
                .stream()
                .chatResponse()
                .filter(chatResponse -> chatResponse!= null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null)
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
        ).subscribeOn(Schedulers.boundedElastic());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/event-stream;charset=UTF-8"))
                .body(content);
    }

    /**
     * 调用 getOrderById 函数获取指定订单信息
     */
    @GetMapping(value = "callGetOrderById", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> callGetOrderById(@RequestParam("question") String question, @RequestParam(value = "orderId", required = false) Long orderId) {
        var content = Flux.defer(() -> this.chatClient.prompt()
                .user(question + "\n请帮我查询订单ID为" + orderId + "的订单信息")
                .system("""
                        你是一只猫娘，每次回答问题后都要加一个喵字。
                        """)
                .tools(orderTools)
                .stream()
                .chatResponse()
                .filter(chatResponse -> chatResponse!= null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null)
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
        ).subscribeOn(Schedulers.boundedElastic());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/event-stream;charset=UTF-8"))
                .body(content);
    }

    /**
     * 调用 findAllOrder 函数获取所有订单信息
     */
    @GetMapping(value = "callFindAllOrder", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> callFindAllOrder(@RequestParam("question") String question) {
        var content = Flux.defer(() -> this.chatClient.prompt()
                .user("请帮我查询所有订单信息\n" + question)
                .system("""
                        你是一只猫娘，每次回答问题后都要加一个喵字。
                        """)
                .tools(orderTools)
                .stream()
                .chatResponse()
                .filter(chatResponse -> chatResponse!= null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null)
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
        ).subscribeOn(Schedulers.boundedElastic());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/event-stream;charset=UTF-8"))
                .body(content);
    }

}
