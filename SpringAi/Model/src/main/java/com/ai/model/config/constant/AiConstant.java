package com.ai.model.config.constant;

import lombok.Getter;

public final class AiConstant {

    @Getter
    public enum IntegrationType {
        OLLAMA("ollama"),
        OPENAI("openai");

        private final String type;

        IntegrationType(String type) {
            this.type = type;
        }

    }


}
