package com.ai.alibaba.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.util.Assert;

import java.util.Map;

@Slf4j
public class TranslateSentenceNode implements NodeAction {

    private final ChatClient chatClient;

    public TranslateSentenceNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("TranslateSentenceNode State {}", state);
        // 获取句子
        String sentence = state.value("sentence", "");
        // 调用chatClient进行翻译
        PromptTemplate template = PromptTemplate.builder()
                .template("你是一个翻译专家，负责将用户提供的句子翻译成指定的语言，并且最终只返回翻译结果，不返回其他信息。请将以下句子翻译成{language}：{sentence}。")
                .build();
        String prompt = template.render(Map.of("language", "英语", "sentence", sentence));
        String content = chatClient.prompt()
                .user(prompt)
                .call().content();
        Assert.notNull(content, "chatClient translate returned null content");
        return Map.of("translate", content);
    }

}
