package com.ai.model.config.model;

import io.netty.util.internal.StringUtil;
import org.springframework.ai.model.Model;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public record AiModelFactory(Map<String, Model<?, ?>> allModels) {

    public Model<?, ?> getModel(String modelType) {
        String type = StringUtil.isNullOrEmpty(modelType) ? "chat" : modelType.trim();
        return switch (type) {
            case "ollama-embedding" -> allModels.get("ollamaEmbeddingModel");
            case "openai-chat" -> allModels.get("openAiChatModel");
            case "openai-embedding" -> allModels.get("openAiEmbeddingModel");
            case "openai-image" -> allModels.get("openAiImageModel");
            case "openai-audio-transcription" -> allModels.get("openAiAudioTranscriptionModel");
            case "openai-audio-speech" -> allModels.get("openAiAudioSpeechClient");
            case "openai-moderation" -> allModels.get("openAiModerationClient");
            default -> allModels.get("ollamaChatModel");
        };
    }

}

