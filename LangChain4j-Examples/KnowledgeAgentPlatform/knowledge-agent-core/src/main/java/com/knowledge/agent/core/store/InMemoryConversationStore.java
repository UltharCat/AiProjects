package com.knowledge.agent.core.store;

import com.knowledge.agent.core.model.ConversationState;
import com.knowledge.agent.core.model.StateContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryConversationStore implements ConversationStore {

    private final Map<Long, StateContext> store = new ConcurrentHashMap<>();

    @Override
    public StateContext getOrCreate(Long userId) {
        return store.computeIfAbsent(userId, id -> StateContext.builder()
                .userId(id)
                .currentState(ConversationState.IDLE)
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public void save(StateContext context) {
        context.setUpdatedAt(LocalDateTime.now());
        store.put(context.getUserId(), context);
    }
}
