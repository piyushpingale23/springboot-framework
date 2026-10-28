# Centralized Logging Pattern

## 1. Definition

The Centralized Logging Pattern collects logs from multiple services into a single central platform.

Instead of checking each microservice's logs separately, developers can search and analyze logs from one place.

## 2. Architecture

```text
 Order Service ------\
 Payment Service -----+----> Log Collector
 User Service --------/           |
                                  v
                           Central Log Store
                                  |
                                  v
                           Search / Dashboard
```

## 3. Why Do We Need It?

Suppose an order fails in a Spring Boot microservices application.

The request passes through:
- API Gateway
- Order Service
- Payment Service
- Notification Service

Without centralized logging, you may need to log in to multiple servers or containers to investigate the failure.

With centralized logging, you can search the logs from all these services in one platform.

## 4. ELK Stack

ELK stands for:

- **Elasticsearch:** Stores and searches indexed log data.
- **Logstash:** Collects, processes, and transforms logs.
- **Kibana:** Provides dashboards and log-search interfaces.

```text
Microservices
      |
      v
Logstash / Log Shippers
      |
      v
Elasticsearch
      |
      v
Kibana Dashboard
```

The exact collection pipeline can vary; lightweight agents such as Filebeat can also ship logs.

## 5. Loki and Grafana

- **Loki:** Aggregates and stores logs, indexing labels rather than the full content of every log line.
- **Grafana:** Provides a UI for searching, exploring, and visualizing logs and other observability data.
- **Log Collector:** An agent such as Grafana Alloy or Promtail in supported deployments forwards logs to Loki.

```text
Microservices
      |
      v
Log Collector
      |
      v
     Loki
      |
      v
   Grafana
```

## 6. Correlation ID

A Correlation ID is a unique identifier used to connect log entries belonging to the same request or business operation across multiple services.

Example:

```text
Client Request
      |
      | correlationId = abc-123
      v
Order Service
      |
      | correlationId = abc-123
      v
Payment Service
      |
      | correlationId = abc-123
      v
Notification Service
```

Example logs:

```text
Order Service:
correlationId=abc-123 Order created

Payment Service:
correlationId=abc-123 Payment successful

Notification Service:
correlationId=abc-123 Email sent
```

Searching for `abc-123` helps you trace the request across all three services.

**Important:** Propagate the correlation ID between services and include it in structured logs. A correlation ID is related to, but not necessarily the same as, a distributed tracing trace ID.

## 7. Example in Spring Boot

Using SLF4J for logging:

```java
private static final Logger log =
        LoggerFactory.getLogger(OrderService.class);

public void createOrder(Long orderId) {
    log.info("Creating order, orderId={}", orderId);
}
```

A production application can use structured logging and include the correlation ID through MDC (Mapped Diagnostic Context).

```
MDC.put("correlationId", correlationId);

try {
    log.info("Processing order");
} finally {
    MDC.remove("correlationId");
}
```

Configure your logging pattern or JSON encoder to include the MDC field. For asynchronous processing, explicitly propagate the context when needed.

## 8. Searching Logs Across Services

Suppose a customer reports that order `101` failed.

You can search centrally using:

```text
correlationId = "abc-123"
```

Or search using business identifiers:

```text
orderId = 101 AND level = ERROR
```

This helps identify:
- Where the request failed.
- Which service generated the error.
- What happened before and after the failure.
- Whether another service timed out or returned an error.

## 9. Advantages

- Logs are available in one central location.
- Makes debugging distributed systems easier.
- Enables searching across services and environments.
- Supports dashboards, alerts, and operational investigations.
- Reduces the need to access each server individually.

## 10. Important Considerations

- Use structured logs such as JSON where practical.
- Propagate correlation IDs consistently.
- Define retention and log rotation policies.
- Avoid logging passwords, tokens, and sensitive personal data.
- Restrict access to logs because they may contain confidential information.
- Monitor log collection failures and storage usage.

## 11. Key Point

**Centralized Logging collects logs from multiple microservices into one platform, while correlation IDs help connect log entries belonging to the same request.**

Common tools include ELK Stack (Elasticsearch, Logstash, Kibana) and Grafana Loki with Grafana.