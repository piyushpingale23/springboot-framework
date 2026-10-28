# Circuit Breaker Pattern in Microservices

## 1. What Is the Circuit Breaker Pattern?

The **Circuit Breaker Pattern** is a microservices resilience design pattern that prevents an application from repeatedly calling a downstream service that is failing or responding slowly.

It temporarily blocks calls to an unhealthy service to protect the calling service from resource exhaustion and cascading failures.

### Example

Consider an e-commerce application with the following services:

- `Order Service`: Processes customer orders.
- `Payment Service`: Processes payments.
- `Inventory Service`: Checks product availability.

When the Order Service calls the Payment Service, a circuit breaker monitors the results of those calls.

If the Payment Service starts failing, the circuit breaker can stop further requests temporarily.

---

## 2. Why Do We Need the Circuit Breaker Pattern?

### 2.1 Normal Operation

Initially, all services are healthy.

```text
Client
   |
   v
Order Service
   |
   v
Payment Service
   |
   v
Payment Successful
```

Example:

- Order Service sends a payment request.
- Payment Service processes the request in 100 ms.
- Payment Service returns a successful response.
- Order Service continues order processing.

Everything works normally.

### 2.2 Downstream Service Becomes Unhealthy

Suppose the Payment Service experiences a database problem.

- Requests take 10 seconds instead of 100 ms.
- Some requests time out.
- Some requests return HTTP 500 errors.
- Incoming requests continue increasing.

Without a circuit breaker, the Order Service continues calling the unhealthy Payment Service.

```text
                   +------------------+
                   |  Order Service   |
                   +------------------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
        Request 1       Request 2       Request 3
             |              |              |
             v              v              v
        +--------------------------------------+
        |       Payment Service                 |
        |       Slow / Unhealthy                |
        +--------------------------------------+
```

This can cause several problems.

1. **Resource exhaustion:** Threads remain occupied while waiting for responses.
2. **Cascading failures:** The Order Service can become unhealthy as well.
3. **Increased latency:** Requests take much longer to complete.
4. **Wasted resources:** Repeated calls are unlikely to succeed while the dependency remains unhealthy.

### 2.3 Introduce the Circuit Breaker

A circuit breaker is placed between the Order Service and Payment Service.

```text
+------------------+
|  Order Service   |
+------------------+
         |
         v
+------------------+
| Circuit Breaker  |
+------------------+
         |
         v
+------------------+
| Payment Service  |
|   Unhealthy      |
+------------------+
```

The circuit breaker monitors call outcomes and durations.

When configured failure conditions are met, it opens the circuit.

Once open, it rejects new calls locally rather than sending them to the downstream service.

The application can then execute a fallback or return a controlled error.

**Important:** A circuit breaker does not repair the downstream service. It protects the calling service while the dependency is unhealthy and periodically checks whether recovery is possible.

---

## 3. Three States of a Circuit Breaker

A circuit breaker normally has three states:

1. `CLOSED`
2. `OPEN`
3. `HALF_OPEN`

### 3.1 State Transition Diagram

```text
                  Normal operation
             +------------------------+
             |                        |
             v                        |
        +---------+                   |
        | CLOSED  |                   |
        +---------+                   |
             |                        |
             | Failure rate or        |
             | slow-call rate         |
             | reaches threshold       |
             v                        |
        +---------+                   |
        |  OPEN   |                   |
        +---------+                   |
             |                        |
             | Waiting period expires |
             v                        |
        +-----------+                 |
        | HALF_OPEN |                 |
        +-----------+                 |
             |                        |
             |                        |
       +-----+-----+                  |
       |           |                  |
       v           v                  |
   Recovery     Failure               |
       |           |                  |
       v           v                  |
    CLOSED        OPEN ----------------+
```

The exact state transitions depend on the configured thresholds and recovery settings.

### 3.2 CLOSED State

**Meaning:** The circuit breaker allows normal calls to the downstream service.

```text
Order Service
      |
      v
Circuit Breaker [CLOSED]
      |
      v
Payment Service
      |
      v
Response
```

The breaker records the outcomes and durations of calls.

For example, suppose the configuration is:

```text
Sliding window size = 10 calls
Minimum calls = 10
Failure threshold = 50%
```

The last ten calls produce these results:

```text
Success: 4
Failure: 6
```

Failure rate:

```text
Failure Rate = (Failed Calls / Total Calls) * 100
             = (6 / 10) * 100
             = 60%
```

Since 60% exceeds the configured 50% threshold, the circuit breaker transitions to `OPEN`.

### 3.3 OPEN State

**Meaning:** New calls to the protected downstream operation are rejected locally.

```text
Request 101 ----+
                |
Request 102 ----+----> Circuit Breaker [OPEN]
                |                |
Request 103 ----+                v
                         Calls Rejected
                         Locally
```

The downstream Payment Service does not receive these rejected calls.

In Resilience4j, an open circuit can cause a `CallNotPermittedException`.

The application can handle this exception through a fallback or other error-handling mechanism.

Benefits:

- Prevents repeated calls to an unhealthy dependency.
- Reduces unnecessary network traffic.
- Avoids wasting resources on calls that are unlikely to succeed.
- Helps prevent cascading failures.

### 3.4 HALF_OPEN State

**Meaning:** The circuit breaker permits a limited number of test calls to determine whether the downstream service has recovered.

Suppose the circuit remains open for 10 seconds.

After the waiting period expires, the breaker enters `HALF_OPEN`.

Example:

```text
Circuit Breaker [HALF_OPEN]

Permitted test calls = 3

Test Call 1 -> Success
Test Call 2 -> Success
Test Call 3 -> Success

Circuit Breaker -> CLOSED
```

If the configured failure or slow-call thresholds are reached during recovery testing, the circuit transitions back to `OPEN`.

The permitted number of test calls and evaluation thresholds are configurable.

**Important:** HALF_OPEN does not mean every incoming request is allowed. Only a limited number of calls are permitted.

---

## 4. Complete Circuit Breaker Lifecycle

Consider the following configuration:

| Configuration             |    Example |
|---------------------------|-----------:|
| Sliding window size       |   10 calls |
| Minimum calls             |         10 |
| Failure threshold         |        50% |
| Open-state waiting period | 10 seconds |
| Permitted HALF_OPEN calls |          3 |

### Step 1: Normal Operation

```text
State = CLOSED

Payment Service responds successfully.
Requests are allowed.
Call outcomes are recorded.
```

### Step 2: Failures Increase

```text
Latest 10 calls:

Success = 4
Failure = 6

Failure Rate = 60%
Threshold = 50%
```

The circuit breaker transitions to `OPEN`.

### Step 3: Protect the Calling Service

```text
State = OPEN

New request
     |
     v
Circuit Breaker
     |
     v
Rejected locally
```

This continues during the configured waiting period.

### Step 4: Test Recovery

```text
State = HALF_OPEN

Limited test calls are allowed.
```

### Step 5: Decide Whether to Recover

```text
If configured health conditions are satisfied:
    HALF_OPEN -> CLOSED

If configured failure or slow-call thresholds are reached:
    HALF_OPEN -> OPEN
```

The circuit breaker is therefore a **state machine that uses call outcomes and time-based transitions to regulate access to a dependency**.

---

## 5. Sliding Window in Circuit Breakers

A circuit breaker does not simply count all failures since the application started.

Resilience4j uses a sliding window to evaluate recent call outcomes.

There are two main types:

1. Count-based sliding window
2. Time-based sliding window

### 5.1 Count-Based Sliding Window

Suppose the sliding window size is 5 calls.

```text
Recent Calls:

[SUCCESS] [FAILURE] [SUCCESS] [FAILURE] [FAILURE]
```

Failure rate:

```text
Failure Rate = (3 / 5) * 100
             = 60%
```

Suppose the next call succeeds.

The oldest measurement leaves the window and the new outcome is added.

```text
Before:

[S] [F] [S] [F] [F]

New Call = Success

After:

[F] [S] [F] [F] [S]
```

The breaker maintains a rolling view of recent behavior.

### 5.2 Time-Based Sliding Window

Instead of considering the last five calls, suppose the breaker monitors the previous 10 seconds.

```text
Time Window:

14:30:00 ---------------------- 14:30:10
             |
             v
       Recent outcomes
```

It aggregates call outcomes within the configured time window. As time advances, older time buckets leave the window.

---

## 6. Failure Rate vs Slow-Call Rate

A circuit breaker can open because requests are failing or because too many requests are taking too long.

### 6.1 Failure Rate

Failure rate represents the percentage of recorded calls classified as failures.

```text
Total calls = 100
Failed calls = 60

Failure rate = 60%
```

If the configured threshold is 50%, the failure-rate condition is met, provided the minimum-call requirement is satisfied.

### 6.2 Slow-Call Rate

Slow-call rate represents the percentage of recorded calls that exceed a configured duration.

Suppose:

```text
Slow-call duration threshold = 3 seconds

Call 1 = 100 ms
Call 2 = 200 ms
Call 3 = 8 seconds
Call 4 = 10 seconds
```

Calls 3 and 4 are considered slow, even if they eventually succeed.

If the slow-call rate reaches its configured threshold, the circuit can open.

---

## 7. Circuit Breaker vs Retry vs Timeout vs Bulkhead

These are different resilience patterns. They can be combined in a microservices' application.

### 7.1 Circuit Breaker

**Purpose:** Prevent repeated calls to an unhealthy dependency.

```text
Question:
Should we call this dependency right now?
```

### 7.2 Retry Pattern

**Purpose:** Repeat a failed operation when a transient failure may be recoverable.

```text
Request
   |
   v
Service Call
   |
   v
Failure
   |
   v
Retry, if permitted
```

Retries should be bounded and should use backoff and jitter where appropriate.

### 7.3 Timeout Pattern

**Purpose:** Limit how long the caller waits for an operation.

```text
Request
   |
   v
Downstream Service
   |
   v
Configured deadline exceeded
   |
   v
Timeout handling
```

A circuit breaker does not replace an effective timeout.

### 7.4 Bulkhead Pattern

**Purpose:** Limit concurrent work so that one dependency cannot consume all available resources.

```text
                 +--------------------+
                 |   Order Service    |
                 +--------------------+
                           |
                           v
                 +--------------------+
                 | Bulkhead Limit     |
                 | Max Concurrent = 20|
                 +--------------------+
                           |
                           v
                 +--------------------+
                 | Payment Service    |
                 +--------------------+
```

### 7.5 Comparison Table

| Pattern         | Primary responsibility                            |
|-----------------|---------------------------------------------------|
| Circuit Breaker | Prevent repeated calls to an unhealthy dependency |
| Retry           | Attempt a failed operation again                  |
| Timeout         | Limit waiting duration                            |
| Bulkhead        | Limit concurrent work                             |
| Rate Limiter    | Limit the rate of incoming or outgoing requests   |

### 7.6 Combining the Patterns

A production application might use:

```text
Order Service
     |
     v
Circuit Breaker
     |
     v
Retry Policy
     |
     v
Timeout
     |
     v
Payment Service
```

---

## 8. Implementing Circuit Breaker in Spring Boot Using Resilience4j

**Resilience4j** is a Java fault-tolerance library that supports circuit breakers, retries, bulkheads, rate limiters, and related resilience capabilities.

For a Spring Boot 3 application, use the compatible Resilience4j Spring Boot 3 starter.

### 8.1 Add Maven Dependency

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>${resilience4j.version}</version>
</dependency>
```

Define `resilience4j.version` in your Maven properties or dependency management.

Choose a release compatible with your Spring Boot version.

### 8.2 Configure application.yml

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2s
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
```

### 8.3 Configuration Explanation

| Property                                | Meaning                                                     |
|-----------------------------------------|-------------------------------------------------------------|
| `slidingWindowType`                     | Defines the sliding-window strategy                         |
| `slidingWindowSize`                     | Evaluates the latest 10 calls                               |
| `minimumNumberOfCalls`                  | Requires at least 10 recorded calls before evaluating rates |
| `failureRateThreshold`                  | Opens the circuit when the failure rate reaches 50%         |
| `slowCallRateThreshold`                 | Opens the circuit when the slow-call rate reaches 50%       |
| `slowCallDurationThreshold`             | Defines a slow call as one exceeding 2 seconds              |
| `waitDurationInOpenState`               | Waits 10 seconds before recovery testing                    |
| `permittedNumberOfCallsInHalfOpenState` | Allows up to 3 test calls in HALF_OPEN                      |

These values are illustrative. Production values should be selected based on latency objectives, traffic patterns, and downstream behavior.

### 8.4 Create a Service Method

Assume `PaymentClient` is an HTTP client that communicates with the downstream Payment Service.

```java
@Service
public class OrderService {

    private final PaymentClient paymentClient;

    public OrderService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @CircuitBreaker(
        name = "paymentService",
        fallbackMethod = "paymentFallback"
    )
    public PaymentResponse makePayment(
            PaymentRequest request) {

        // Call the downstream Payment Service.
        return paymentClient.processPayment(request);
    }

    public PaymentResponse paymentFallback(
            PaymentRequest request,
            Throwable exception) {

        // Return a controlled response.
        return new PaymentResponse(
            false,
            "Payment service is temporarily unavailable"
        );
    }
}
```

The example assumes `PaymentRequest`, `PaymentResponse`, and `PaymentClient` are application-defined classes.

The fallback method must have a compatible return type and accept the original parameters followed by the exception parameter.

For production code, distinguish between a definite payment failure and an unknown payment outcome.

### 8.5 Runtime Flow

```text
Client
   |
   v
OrderController
   |
   v
OrderService.makePayment()
   |
   v
Circuit Breaker
   |
   +---- CLOSED ----------------------+
   |                                  |
   v                                  |
PaymentClient                         |
   |                                  |
   v                                  |
Payment Service                       |
   |                                  |
   v                                  |
Response / Exception                  |
   |                                  |
   +----------------------------------+
                    |
                    v
              Result handling


When OPEN:

OrderService.makePayment()
   |
   v
Circuit Breaker
   |
   v
CallNotPermittedException
   |
   v
Fallback handling
```

The circuit breaker monitors the protected operation. When the circuit is open, the downstream operation is not invoked.

---

## 9. Interview Questions and Answers

### Q1. What is the Circuit Breaker Pattern?

It is a resilience pattern that temporarily blocks calls to an unhealthy downstream service to prevent repeated failures and cascading failures.

### Q2. What are the three states of a circuit breaker?

- `CLOSED`: Calls are allowed and outcomes are monitored.
- `OPEN`: New protected calls are rejected locally.
- `HALF_OPEN`: A limited number of calls are permitted to test recovery.

### Q3. What is the difference between OPEN and CLOSED?

`CLOSED` allows calls to the downstream service. `OPEN` blocks new calls to the protected operation.

### Q4. Why is HALF_OPEN required?

It permits limited recovery testing instead of immediately allowing unrestricted traffic after the waiting period.

### Q5. What is a sliding window?

A sliding window tracks recent call outcomes using either a fixed number of calls or a fixed time interval.

### Q6. Can a successful call count as a slow call?

Yes. A successful call can be considered slow if its duration exceeds the configured threshold.

### Q7. Does a circuit breaker retry failed requests?

No. Circuit breakers and retry mechanisms have separate responsibilities.

### Q8. Does a circuit breaker guarantee that the downstream service will recover?

No. It protects the calling service and permits recovery testing. It does not repair the dependency.

### Q9. What happens when a call is made while the circuit is OPEN in Resilience4j?

The protected operation is rejected locally, typically with `CallNotPermittedException`. Fallback handling may provide an alternative response.

### Q10. What is the difference between Circuit Breaker and Bulkhead?

A circuit breaker controls calls based on observed failures or slow responses. A bulkhead limits concurrent work to isolate resource consumption.

### Q11. Does a circuit breaker replace a timeout?

No. A timeout limits how long an operation can take from the caller's perspective. A circuit breaker uses call outcomes and durations to decide whether to allow future calls.

### Q12. Can circuit breakers in different service instances have different states?

Yes. Typical in-process circuit breakers maintain local state and observe different request histories.

### Q13. What library is commonly used for circuit breakers in Spring Boot?

Resilience4j is a common choice. It supports circuit breakers, retries, bulkheads, rate limiters, and other resilience mechanisms.

### Q14. Why should payment requests not be blindly retried?

A timed-out request may already have processed the payment. Retrying without idempotency or reconciliation can cause duplicate charges.

### Q15. What is cascading failure?

Cascading failure occurs when a failure in one service causes dependent services to become overloaded or fail as well.

A circuit breaker can reduce this risk by stopping repeated calls to an unhealthy dependency.

---

## 10. Final Summary

The Circuit Breaker Pattern protects microservices from repeatedly calling a failing or slow dependency.

Remember these points:

1. `CLOSED` allows calls and monitors their outcomes.
2. `OPEN` rejects new protected calls locally.
3. `HALF_OPEN` allows limited test calls to evaluate recovery.
4. Sliding windows evaluate recent failures and slow calls.
5. Failure-rate and slow-call-rate thresholds are configurable.
6. Fallbacks provide alternative responses when appropriate.
7. Retry, timeout, bulkhead, and circuit breaker patterns solve different problems.
8. Resilience4j is a common circuit-breaker implementation for Spring Boot.
9. Circuit breakers in different service instances usually maintain independent state.
10. Payment operations require idempotency and reconciliation in addition to resilience mechanisms.

### Recommended Learning Sequence

1. Circuit Breaker Pattern
2. Retry Pattern
3. Timeout Pattern
4. Bulkhead Pattern
5. Rate Limiter Pattern
6. Combining resilience patterns in Spring Boot
