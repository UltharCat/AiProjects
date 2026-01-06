package com.ai.alibaba.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/graph")
public class GraphController {

    private final CompiledGraph quickStartGraph;

    private final CompiledGraph sentenceCreateGraph;

    public GraphController(@Qualifier("quickStartGraph") CompiledGraph quickStartGraph,
                           @Qualifier("sentenceCreateGraph") CompiledGraph sentenceCreateGraph) {
        this.quickStartGraph = quickStartGraph;
        this.sentenceCreateGraph = sentenceCreateGraph;
    }

    @GetMapping("/quickStartGraph")
    public ResponseEntity<?> quickStartGraph() {
        return quickStartGraph.invoke(new HashMap<>())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/sentenceCreateGraph")
    public ResponseEntity<Map<String, Object>> sentenceCreateGraph(@RequestParam("handle") String handle) {
        return ResponseEntity.ok(
                sentenceCreateGraph.invoke(Map.of("handle", handle))
                        .map(OverAllState::data)
                        .orElseGet(HashMap::new)
        );
    }

}
