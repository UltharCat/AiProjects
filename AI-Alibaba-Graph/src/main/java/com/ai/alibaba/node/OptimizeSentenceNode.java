package com.ai.alibaba.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

@Slf4j
public class OptimizeSentenceNode implements NodeAction {

    private final ChatClient chatClient;

    public OptimizeSentenceNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("OptimizeSentenceNode State {}", state);
        // 调用chatClient进行优化
        PromptTemplate template = PromptTemplate.builder()
                .template("""
                        你是一个造句优化专家，用户提供句子和积极性评分，请根据评分对句子进行优化，降低句子的积极性，
                        并且最终只返回一个 map/json 格式，包含两个字段：sentence（字符串）和 positiveScore（整数，范围0到10），评分只返回整数数值，不返回其它解释。
                        请根据以下句子进行优化：{sentence}，当前积极性评分为：{positiveScore}。
                        """)
                .build();
        String prompt = template.render(state.data());
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        return JSON.parseObject(content, new TypeReference<>(){});
    }

}
