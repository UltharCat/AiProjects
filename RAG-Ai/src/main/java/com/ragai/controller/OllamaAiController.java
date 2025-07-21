package com.ragai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ollamaAi")
public class OllamaAiController {

    private final ChatClient chatClient;

    public OllamaAiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();

    }

    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }

}
