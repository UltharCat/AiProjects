package com.knowledge.agent.rag.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Value("${langchain4j.open-ai.embedding-model.base-url}")
    private String BASE_URL;
    @Value("${langchain4j.open-ai.embedding-model.api-key}")
    private String API_KEY;
    @Value("${langchain4j.open-ai.embedding-model.model-name}")
    private String MODEL_NAME;
    @Value("${langchain4j.open-ai.embedding-model.dimension:1024}")
    private Integer DIMENSION;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName(MODEL_NAME)
                .dimensions(DIMENSION)
                .build();
    }

}
