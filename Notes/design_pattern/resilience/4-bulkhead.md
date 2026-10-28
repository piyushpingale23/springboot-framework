# Bulkhead Pattern in Microservices

## 1. What Is the Bulkhead Pattern?

The **Bulkhead Pattern** isolates resources so that a failure or overload in one component does not consume all the resources needed by the rest of the application.

It is inspired by the compartments in a ship that prevent flooding in one section from sinking the entire vessel.

## 2. Why Do We Need Bulkhead?

Suppose an Order Service calls three downstream services:

- Payment Service
- Inventory Service
- Recommendation Service

If the Recommendation Service becomes very slow, its requests may consume too many threads.

Without isolation, Payment and Inventory operations may also become unavailable.

```text
             Order Service
                   |
          +--------+--------+
          |        |        |
          v        v        v
       Payment  Inventory Recommendation
       Service   Service     Service
                             |
                             v
                        Slow Requests
                             |
                             v
                     Resources Exhausted
```

## 3. How Does Bulkhead Work?

A bulkhead allocates separate or limited resources to different operations.

```text
                 Order Service
                       |
          +------------+------------+
          |            |            |
          v            v            v
     +----------+ +----------+ +----------+
     | Payment  | |Inventory | |Recommend.|
     | Limit: 20| | Limit: 20| | Limit: 5 |
     +----------+ +----------+ +----------+
```

If Recommendation requests reach their limit, additional requests to that protected operation can be rejected without consuming unlimited resources.

Payment and Inventory retain their separately allocated capacity.

The numbers above are illustrative concurrency limits, not recommended defaults.

## 4. Types of Bulkhead

| Type                 | Description                                                   |
|----------------------|---------------------------------------------------------------|
| Semaphore Bulkhead   | Limits the number of concurrent executions                    |
| Thread-Pool Bulkhead | Uses a dedicated thread pool and queue for isolated execution |

A semaphore bulkhead limits concurrency without inherently creating a dedicated thread pool.

A thread-pool bulkhead isolates execution using a separate pool and its configured queue capacity.

## 5. Spring Boot Using Resilience4j

```java
@Service
public class RecommendationService {

    @Bulkhead(
        name = "recommendationService",
        type = Bulkhead.Type.SEMAPHORE
    )
    public List<String> getRecommendations() {

        // Call the downstream Recommendation Service.
        return recommendationClient.fetch();
    }
}
```

Example configuration:

```yaml
resilience4j:
  bulkhead:
    instances:
      recommendationService:
        maxConcurrentCalls: 5
        maxWaitDuration: 0
```

The example assumes `recommendationClient` is an injected client and the compatible Resilience4j Spring Boot integration is configured.

Here:

- At most 5 concurrent executions are permitted.
- Additional calls do not wait for a semaphore permit when `maxWaitDuration` is zero.
- Calls that exceed the bulkhead limit are rejected.

## 6. Bulkhead vs Circuit Breaker

| Bulkhead                                    | Circuit Breaker                                             |
|---------------------------------------------|-------------------------------------------------------------|
| Limits concurrent resource usage            | Controls calls based on observed failures or slowness       |
| Protects resource capacity                  | Protects against repeatedly calling an unhealthy dependency |
| Can reject calls when capacity is exhausted | Can reject calls when the circuit is OPEN                   |

These patterns can be used together.

## 7. Important Considerations

- Set concurrency limits according to available resources and downstream capacity.
- Choose semaphore or thread-pool isolation based on the workload.
- Configure queue capacity carefully for thread-pool bulkheads.
- Handle rejected calls gracefully.
- A bulkhead does not detect whether a downstream service is healthy.
- Combine it with timeouts and circuit breakers where appropriate.

## 8. Summary

The Bulkhead Pattern isolates resources to prevent one overloaded operation from exhausting the entire application.

**Remember:** Bulkhead means *limit and isolate resource consumption*.
