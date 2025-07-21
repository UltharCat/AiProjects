package com.ragbase.service;

public interface OllamaBaseService {

    /**
     * 与Ollama进行对话
     * @param prompt
     * @return
     */
    String chatWithOllama(String prompt);

}
