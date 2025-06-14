package com.rrp.experiment.controller;

import com.rrp.experiment.service.PerformanceTestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceTestController {
    private final PerformanceTestService performanceTestService;

    public PerformanceTestController(PerformanceTestService performanceTestService) {
        this.performanceTestService = performanceTestService;
    }

    @PostMapping("/virtual-threads")
    public List<String> testVirtualThreads(@RequestParam(name = "numberOfTasks", defaultValue = "100") int numberOfTasks) {
        return performanceTestService.testWithVirtualThreads(numberOfTasks);
    }

    @PostMapping("/traditional-threads")
    public List<String> testTraditionalThreads(@RequestParam(name = "numberOfTasks", defaultValue = "100") int numberOfTasks) {
        return performanceTestService.testWithTraditionalThreads(numberOfTasks);
    }
} 