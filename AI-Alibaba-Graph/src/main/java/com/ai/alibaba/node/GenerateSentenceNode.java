package com.ai.alibaba.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

@Slf4j
public class GenerateSentenceNode implements NodeAction {

    private final ChatClient chatClient;

    public GenerateSentenceNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("overAllState {}", state);
        // 获取state内参数
        String handle = state.value("handle", "");
        // 调用chatClient生成句子
        PromptTemplate template = PromptTemplate.builder()
                .template("你是一个造句专家，要根据用户提供的关键词，按照关键词的语种进行相对应语种的造句，并且最终只返回造句，不返回其他信息。请根据以下关键词造句：{handle}。")
                .build();
        String prompt = template.render(Map.of("handle", handle));

        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return Map.of("sentence", content);
    }

}
