package com.base.controller;

import com.base.service.OllamaBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ollamaBase")
public class OllamaBaseController {

    private final OllamaBaseService ollamaBaseService;

    public OllamaBaseController(OllamaBaseService ollamaBaseService) {
        this.ollamaBaseService = ollamaBaseService;
    }


    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        return ollamaBaseService.chatWithOllama(prompt);
    }

}
