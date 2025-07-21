package com.ragbase.service.impl.ollamaBase;

import com.ragbase.service.OllamaBaseService;
import org.springframework.beans.factory.annotation.Value;

public abstract class OllamaClient implements OllamaBaseService {

    @Value("${spring.ai.ollama.base-url}")
    protected String OLLAMA_URL;

    @Value("${spring.ai.ollama.chat.options.model}")
    protected String OLLAMA_MODEL;

    public String chatWithOllama(String prompt){
        // This method should be implemented by subclasses to handle the actual chat logic
        // using the OLLAMA_URL and OLLAMA_MODEL.
        return null;
    }

}
