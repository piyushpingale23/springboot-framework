# Resilience4j

## 1. What is Resilience4j?

Resilience4j is a lightweight fault-tolerance library for Java applications, commonly used with Spring Boot microservices.

It helps applications remain stable when dependent services are slow, unavailable, or overloaded.

## 2. Four Important Resilience Patterns

- **Retry:** Retries a failed operation.
- **Timeout:** Stops waiting when an operation exceeds a specified time limit.
- **Bulkhead:** Limits concurrent operations to prevent resource exhaustion.
- **Circuit Breaker:** Temporarily blocks calls to an unhealthy service.

## 3. Common Resilience4j Annotations

```
@Retry(name = "paymentService")
@CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
@Bulkhead(name = "paymentService")
```

**Timeout:** Typically configured at the HTTP client or other operation level, rather than through a universal Resilience4j `@Timeout` annotation.

## 4. Dependency

For Spring Boot 3.x, a common dependency is:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>${resilience4j.version}</version>
</dependency>
```

Use a compatible Resilience4j version and configure Spring Boot AOP as required by the integration.

## 5. Quick Summary

| Pattern         | Purpose                           |
|-----------------|-----------------------------------|
| Retry           | Try again                         |
| Timeout         | Stop waiting                      |
| Bulkhead        | Limit resource usage              |
| Circuit Breaker | Stop calling an unhealthy service |

**Key Point:** Resilience4j provides reusable mechanisms to improve fault tolerance and stability in Spring Boot microservices.