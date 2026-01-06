package com.ai.alibaba.config;

import com.ai.alibaba.node.GenerateSentenceNode;
import com.ai.alibaba.node.SentenceAudioSynthesisNode;
import com.ai.alibaba.node.TranslateSentenceNode;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.MergeStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Slf4j
public class GraphConfig {

    @Bean("quickStartGraph")
    public CompiledGraph quickStartGraph() throws Exception {
        // 定义图
        StateGraph stateGraph = new StateGraph("quickStartGraph",
                () -> Map.of("input", new AppendStrategy(),
                        "output", new MergeStrategy()));
        // 定义节点
        stateGraph.addNode("node1", AsyncNodeAction.node_async(overAllState -> {
            log.info("overAllState1 {}", overAllState);
            return Map.of("input", "input_from_node1",
                    "output", "output_from_node1");
        }));
        stateGraph.addNode("node2", AsyncNodeAction.node_async(overAllState -> {
            log.info("overAllState2 {}", overAllState);
            return Map.of("input", "input_from_node2",
                    "output", "output_from_node2");
        }));

        // 定义边
        stateGraph.addEdge(StateGraph.START, "node1");
        stateGraph.addEdge("node1", "node2");
        stateGraph.addEdge("node2", StateGraph.END);

        return stateGraph.compile();
    }

    @Bean("sentenceCreateGraph")
    public CompiledGraph sentenceCreateGraph(ChatClient.Builder builder,
                                             @Qualifier("dashScopeSpeechSynthesisModel") TextToSpeechModel speechSynthesisModel
    ) throws Exception {
        // 定义图
        StateGraph stateGraph = new StateGraph("SentenceCreateGraph",
                () -> Map.of("handle", new ReplaceStrategy()));
        // 定义节点
        stateGraph.addNode("generateSentenceNode", AsyncNodeAction.node_async(new GenerateSentenceNode(builder)));
        stateGraph.addNode("translateSentenceNode", AsyncNodeAction.node_async(new TranslateSentenceNode(builder)));
        stateGraph.addNode("sentenceAudioSynthesisNode", AsyncNodeAction.node_async(new SentenceAudioSynthesisNode(speechSynthesisModel)));

        // 定义边
        stateGraph.addEdge(StateGraph.START, "generateSentenceNode");
        stateGraph.addEdge("generateSentenceNode", "translateSentenceNode");
        stateGraph.addEdge("translateSentenceNode", "sentenceAudioSynthesisNode");
        stateGraph.addEdge("sentenceAudioSynthesisNode", StateGraph.END);

        return stateGraph.compile();
    }

}
