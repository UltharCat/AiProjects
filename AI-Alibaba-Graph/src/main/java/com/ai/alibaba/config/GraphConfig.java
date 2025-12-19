package com.ai.alibaba.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.MergeStrategy;
import lombok.extern.slf4j.Slf4j;
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

}
