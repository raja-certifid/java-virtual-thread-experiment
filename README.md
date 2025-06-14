# java-virtual-thread-experiment

This project demonstrates the performance differences between Java Virtual Threads and Traditional Platform Threads in a Spring Boot application, particularly focusing on I/O-bound operations.

## Overview

The project implements two different approaches to handle concurrent tasks:
1. Virtual Threads (Project Loom)
2. Traditional Platform Threads with a fixed thread pool

Each implementation is tested with the same workload to compare their performance characteristics.

## Prerequisites

- Java 21 or later
- Gradle 8.x or later
- Spring Boot 3.2.x

## Project Structure

```
src/main/java/com/rrp/experiment/
├── controller/
│   └── PerformanceTestController.java
├── service/
│   └── PerformanceTestService.java
└── DemoApplication.java
```

## Key Components

### PerformanceTestController
- Exposes two endpoints for testing:
  - `/api/performance/virtual-threads`
  - `/api/performance/traditional-threads`
- Both endpoints accept a `numberOfTasks` parameter

### PerformanceTestService
- Implements two methods for task execution:
  - `testWithVirtualThreads`: Uses `Executors.newVirtualThreadPerTaskExecutor()`
  - `testWithTraditionalThreads`: Uses `Executors.newFixedThreadPool(4)`
- Each task performs 3 HTTP calls to simulate I/O operations
- Metrics are collected using Micrometer for performance analysis

## Performance Testing Results

### Test Configuration
- Number of tasks: 20
- Tasks per implementation: 3 HTTP calls per task
- Traditional thread pool size: 4 threads

### Results
1. **Virtual Threads Implementation**
   - Execution time: ~80.8 seconds
   - Each task gets its own virtual thread
   - True concurrency for all tasks
   - Efficient handling of I/O operations

2. **Traditional Threads Implementation**
   - Execution time: ~104.3 seconds
   - Limited to 4 platform threads
   - Tasks are queued and processed in batches
   - Thread reuse pattern visible in output

### Key Findings
1. **Performance Difference**
   - Virtual threads completed the workload ~23.5 seconds faster
   - This demonstrates the efficiency of virtual threads for I/O-bound operations

2. **Resource Utilization**
   - Virtual threads: Lightweight, can handle many concurrent tasks
   - Traditional threads: Resource-intensive, limited by thread pool size

3. **Concurrency Model**
   - Virtual threads: True concurrency with minimal overhead
   - Traditional threads: Thread pool-based concurrency with queuing

## Additional Experiment: Disabling Virtual Threads at the Gradle/JVM Level

To further validate the impact of virtual threads, we disabled the JVM argument `-Dspring.threads.virtual.enabled=true` in the `build.gradle` file and reran the same tests.

### Results with Virtual Threads Disabled
- **Virtual Threads Endpoint (now using platform threads):**
  - Execution time (MAX): ~79.7 seconds (for 20 tasks, 3 HTTP calls each)
- **Traditional Threads Endpoint:**
  - Execution time (MAX): ~75.6 seconds (for 20 tasks, 3 HTTP calls each)

### Key Observations
- Both endpoints now use platform threads, so their performance is very similar.
- The advantage of virtual threads (true concurrency and lower execution time) is lost when the JVM is not configured to enable them.
- The results confirm that the Gradle/JVM configuration directly impacts whether your code uses virtual threads or falls back to platform threads—even if your code is written to use virtual threads.

### Conclusion
Enabling virtual threads at the JVM level is essential to realize their performance benefits. Without the correct configuration, your application will default to platform threads, resulting in similar performance for both concurrency models.

## Running the Tests

1. Start the application:
```bash
./gradlew bootRun
```

2. Test virtual threads:
```bash
curl -X POST "http://localhost:8080/api/performance/virtual-threads?numberOfTasks=20"
```

3. Test traditional threads:
```bash
curl -X POST "http://localhost:8080/api/performance/traditional-threads?numberOfTasks=20"
```

4. Check metrics:
```bash
curl "http://localhost:8080/actuator/metrics/virtual.thread.execution.time"
curl "http://localhost:8080/actuator/metrics/traditional.thread.execution.time"
```

## Conclusion

The test results demonstrate that virtual threads are particularly effective for I/O-bound operations, where they can handle many concurrent tasks without the overhead of platform threads. The traditional thread pool, while still efficient, is constrained by its fixed size and needs to queue tasks when all threads are busy.

This makes virtual threads an excellent choice for:
- Web applications with many concurrent users
- Microservices with frequent I/O operations
- Applications requiring high concurrency with minimal resource overhead

## Future Improvements

1. Test with larger numbers of tasks (100+)
2. Add more complex I/O operations
3. Implement different thread pool sizes for traditional threads
4. Add CPU-bound workload comparison
5. Measure memory usage differences