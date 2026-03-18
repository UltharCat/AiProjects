package com.knowledge.agent.core.store;

import com.knowledge.agent.core.model.ConversationState;
import com.knowledge.agent.core.model.StateContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisConversationStoreTest {

    @Test
    void shouldFallbackToInMemoryStoreWhenRedisIsUnavailable() {
        RedisConversationStore store = new RedisConversationStore(new InMemoryConversationStore());

        StateContext context = store.getOrCreate(1L);
        context.setCurrentState(ConversationState.TEACHING);
        store.save(context);

        assertEquals(ConversationState.TEACHING, store.getOrCreate(1L).getCurrentState());
    }
}
