package com.base.service;

public interface OllamaBaseService {

    /**
     * 与Ollama进行对话
     * @param prompt 用户输入
     * @return assistant 输出
     */
    String chatWithOllama(String prompt);

}
