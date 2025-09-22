package com.ai.alibaba.config.model;

import io.netty.util.internal.StringUtil;
import org.springframework.ai.model.Model;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public record AiModelFactory(Map<String, Model<?, ?>> allModels) {

    public Model<?, ?> getModel(String modelName) {
        String type = StringUtil.isNullOrEmpty(modelName) ? "chat" : modelName.trim();
        return switch (type) {
            case "dashscope-image" -> allModels.get("dashScopeImageModel");
            case "dashscope-audio-transcription" -> allModels.get("dashScopeAudioTranscriptionModel");
            case "dashscope-audio-synthesis" -> allModels.get("dashScopeSpeechSynthesisModel");
            case "ollama-chat" -> allModels.get("ollamaChatModel");
            case "openai-chat" -> allModels.get("openaiChatModel");
            default -> allModels.get("dashscopeChatModel");
        };
    }

}

