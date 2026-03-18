package com.knowledge.agent.core.llm;

import cn.hutool.core.util.StrUtil;
import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.UserProfileDTO;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Optional LangChain4j runtime used to upgrade deterministic replies when model access is configured.
 */
@Slf4j
@Component
public class LangChain4jAgentRuntime {

    @Value("${knowledge-agent.llm.enabled:false}")
    private boolean enabled;

    @Value("${knowledge-agent.llm.base-url:}")
    private String baseUrl;

    @Value("${knowledge-agent.llm.api-key:}")
    private String apiKey;

    @Value("${knowledge-agent.llm.model-name:gpt-4o-mini}")
    private String modelName;

    @Value("${knowledge-agent.llm.timeout:PT20S}")
    private Duration timeout;

    private volatile ChatModel chatLanguageModel;

    public Optional<String> generateTeachingReply(String instruction,
                                                  UserProfileDTO profile,
                                                  List<KnowledgeDTO> knowledgeList,
                                                  String query) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        String prompt = """
                You are a tutoring agent.
                Instruction: %s
                Learning style: %s
                User query: %s
                Knowledge context:
                %s
                Please answer clearly and practically in no more than 6 sentences.
                """.formatted(
                instruction,
                profile == null ? "SOCRATIC" : StrUtil.blankToDefault(profile.getLearningStyle(), "SOCRATIC"),
                query,
                formatKnowledge(knowledgeList)
        );
        return generate(prompt);
    }

    public Optional<String> summarize(String topic, String recentDialog) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        String prompt = """
                Summarize the following learning dialog into a reusable knowledge note.
                Topic: %s
                Recent dialog:
                %s
                Return a concise structured summary with key concepts and one review hint.
                """.formatted(topic, recentDialog);
        return generate(prompt);
    }

    private Optional<String> generate(String prompt) {
        try {
            return Optional.ofNullable(getOrCreateModel().chat(prompt));
        } catch (Exception ex) {
            log.warn("LangChain4j runtime call failed, falling back to rule-based response.", ex);
            return Optional.empty();
        }
    }

    private boolean isConfigured() {
        return enabled && StrUtil.isNotBlank(apiKey) && StrUtil.isNotBlank(baseUrl);
    }

    private ChatModel getOrCreateModel() {
        if (chatLanguageModel == null) {
            synchronized (this) {
                if (chatLanguageModel == null) {
                    chatLanguageModel = OpenAiChatModel.builder()
                            .baseUrl(baseUrl)
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .timeout(timeout)
                            .build();
                }
            }
        }
        return chatLanguageModel;
    }

    private String formatKnowledge(List<KnowledgeDTO> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            return "- no knowledge found";
        }
        return knowledgeList.stream()
                .map(dto -> "- " + StrUtil.blankToDefault(dto.getSummary(), "No summary"))
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("- no knowledge found");
    }
}
