package com.ai.alibaba.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private final CompiledGraph compiledGraph;


    public GraphController(CompiledGraph compiledGraph) {
        this.compiledGraph = compiledGraph;
    }

    @GetMapping("/quickStartGraph")
    public Flux<?> quickStartGraph() {
        return Flux.defer(compiledGraph::stream).subscribeOn(Schedulers.boundedElastic());
    }


}
