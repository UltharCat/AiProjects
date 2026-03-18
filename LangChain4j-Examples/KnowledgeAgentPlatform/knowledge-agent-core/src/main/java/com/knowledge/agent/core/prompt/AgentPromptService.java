package com.knowledge.agent.core.prompt;

import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.core.model.ConversationState;
import org.springframework.stereotype.Component;

@Component
public class AgentPromptService {

    public String buildInstruction(ConversationState state, String learningStyle) {
        String style = learningStyle == null || learningStyle.isBlank() ? "SOCRATIC" : learningStyle;
        return switch (state) {
            case IDLE -> "The agent is idle. Detect whether the user needs chat, teaching, summary, or review.";
            case TEACHING -> "The agent is in teaching mode. Follow the user's learning style %s and explain step by step.".formatted(style);
            case REVIEW -> "The agent is in review mode. Ask recall-first questions and update memory after feedback.";
            case SUMMARY -> "The agent is in summary mode. Turn recent dialog into a reusable knowledge summary.";
        };
    }

    public String buildInstruction(ConversationState state, UserProfileDTO userProfile) {
        return buildInstruction(state, userProfile == null ? null : userProfile.getLearningStyle());
    }
}
