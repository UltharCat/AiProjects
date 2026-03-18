package com.knowledge.agent.core.store;

import com.knowledge.agent.core.model.StateContext;

public interface ConversationStore {

    StateContext getOrCreate(Long userId);

    void save(StateContext context);
}
