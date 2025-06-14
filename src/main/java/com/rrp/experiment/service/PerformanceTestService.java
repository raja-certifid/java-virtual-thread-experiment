package com.rrp.experiment.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PerformanceTestService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestService.class);
    private final MeterRegistry meterRegistry;
    private final Timer virtualThreadTimer;
    private final Timer traditionalThreadTimer;
    private final RestTemplate restTemplate;
    
    public PerformanceTestService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.restTemplate = new RestTemplate();
        this.virtualThreadTimer = Timer.builder("virtual.thread.execution.time")
                .description("Time taken to execute tasks using virtual threads")
                .register(meterRegistry);
        this.traditionalThreadTimer = Timer.builder("traditional.thread.execution.time")
                .description("Time taken to execute tasks using traditional threads")
                .register(meterRegistry);
    }
    
    public List<String> testWithVirtualThreads(int numberOfTasks) {
        logger.info("Starting virtual threads test with {} tasks", numberOfTasks);
        long startTime = System.currentTimeMillis();
        List<String> results = virtualThreadTimer.record(() -> {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                return IntStream.range(0, numberOfTasks)
                    .mapToObj(i -> executor.submit(() -> {
                        logger.debug("Virtual thread {} starting", i);
                        try {
                            // Make multiple HTTP calls to simulate I/O operations
                            for (int j = 0; j < 3; j++) {
                                String response = restTemplate.getForObject(
                                    "https://httpbin.org/delay/1", String.class);
                                logger.debug("Virtual thread {} completed HTTP call {}", i, j);
                            }
                            String result = String.format("Task %d completed on virtual thread %s", 
                                i, Thread.currentThread().getName());
                            logger.debug("Virtual thread {} completed", i);
                            return result;
                        } catch (Exception e) {
                            logger.error("Virtual thread {} failed", i, e);
                            throw new RuntimeException("Task failed", e);
                        }
                    }))
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            logger.error("Error in virtual thread task", e);
                            throw new RuntimeException("Error executing task", e);
                        }
                    })
                    .collect(Collectors.toList());
            }
        });
        long endTime = System.currentTimeMillis();
        logger.info("Virtual threads test completed in {} ms", (endTime - startTime));
        return results;
    }
    
    public List<String> testWithTraditionalThreads(int numberOfTasks) {
        logger.info("Starting traditional threads test with {} tasks", numberOfTasks);
        long startTime = System.currentTimeMillis();
        List<String> results = traditionalThreadTimer.record(() -> {
            // Use a smaller thread pool to demonstrate the difference
            try (var executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4)) {
                logger.info("Using thread pool with {} threads", executor.getMaximumPoolSize());
                return IntStream.range(0, numberOfTasks)
                    .mapToObj(i -> executor.submit(() -> {
                        logger.debug("Traditional thread {} starting", i);
                        try {
                            // Make multiple HTTP calls to simulate I/O operations
                            for (int j = 0; j < 3; j++) {
                                String response = restTemplate.getForObject(
                                    "https://httpbin.org/delay/1", String.class);
                                logger.debug("Traditional thread {} completed HTTP call {}", i, j);
                            }
                            String result = String.format("Task %d completed on platform thread %s", 
                                i, Thread.currentThread().getName());
                            logger.debug("Traditional thread {} completed", i);
                            return result;
                        } catch (Exception e) {
                            logger.error("Traditional thread {} failed", i, e);
                            throw new RuntimeException("Task failed", e);
                        }
                    }))
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            logger.error("Error in traditional thread task", e);
                            throw new RuntimeException("Error executing task", e);
                        }
                    })
                    .collect(Collectors.toList());
            }
        });
        long endTime = System.currentTimeMillis();
        logger.info("Traditional threads test completed in {} ms", (endTime - startTime));
        return results;
    }
} 