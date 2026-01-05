package com.ai.alibaba.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Optional;

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
    public Flux<?> quickStartGraph() {
        return Flux.defer(quickStartGraph::stream).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/sentenceCreateGraph")
    public Flux<?> sentenceCreateGraph(@RequestParam("handle") String handle) {
        return Flux.defer(
                () -> sentenceCreateGraph.stream(Map.of("handle", handle))
        ).subscribeOn(Schedulers.boundedElastic());
    }

}
