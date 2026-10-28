# Microservices Patterns

# 1. Microservices Patterns — Learning Roadmap

We will learn the patterns in the following order.

## A. API & Communication Patterns

### 1. API Gateway Pattern

* Single entry point for clients
* Routing
* Authentication
* Authorization
* Rate limiting
* Request aggregation
* SSL/TLS termination
* Logging
* Load balancing integration

### 2. Service Discovery Pattern

* Service Registry
* Service Registration
* Service Discovery
* Client-side discovery
* Server-side discovery
* Eureka
* Kubernetes service discovery

### 3. Inter-Service Communication Pattern

* Synchronous communication
* REST
* gRPC
* Asynchronous communication
* Kafka
* RabbitMQ
* When to use synchronous vs asynchronous communication

### 4. Aggregator Pattern

* Multiple service calls
* Combining responses
* API composition
* Client-specific responses

---

# B. Resilience Patterns

### 5. Circuit Breaker Pattern

* Cascading failure
* CLOSED state
* OPEN state
* HALF-OPEN state
* Failure threshold
* Recovery
* Resilience4j

### 6. Retry Pattern

* Temporary failures
* Retry attempts
* Exponential backoff
* Jitter
* Retry + Circuit Breaker

### 7. Timeout Pattern

* Remote call timeout
* Connection timeout
* Read timeout
* Preventing resource exhaustion

---

# C. Data Management Patterns

### 8. Database per Service Pattern

* Database ownership
* Data isolation
* Service autonomy
* Why shared database is problematic

### 9. Saga Pattern

* Distributed transactions
* Local transactions
* Choreography
* Orchestration
* Compensating transactions
* Failure scenarios

### 10. Event Sourcing Pattern ⭐⭐⭐⭐

* Events instead of current state
* Event store
* Rebuilding state
* Audit history
* Advantages/disadvantages

---

# D. Scalability & Availability Patterns

### 12. Load Balancing Pattern

* Horizontal scaling
* Multiple service instances
* Round Robin
* Least Connections
* Weighted routing
* Health checks

### 13. Stateless Service Pattern

* Stateless applications
* Horizontal scaling
* Session management
* Redis/JWT/external state

### 14. Idempotency Pattern

* Duplicate requests
* Payment APIs
* Retry safety
* Idempotency key
* Exactly-once business effect

---

# E. Security & Traffic Management

### 15. Rate Limiting Pattern

* Request throttling
* Fixed Window
* Sliding Window
* Token Bucket
* Leaky Bucket
* HTTP 429

---

# F. Migration & Infrastructure Patterns

### 16. Sidecar Pattern

* Helper container/process
* Logging
* Networking
* Security
* Service mesh

---

# G. Observability Patterns

### 17. Centralized Logging Pattern

* Central log collection
* ELK
* Loki
* Grafana
* Correlation ID
* Searching logs across services

---

# 2. Recommended Learning Priority

### Most frequently asked

```text
1. API Gateway
2. Service Discovery
3. Circuit Breaker
4. Saga
5. Database per Service
6. Inter-Service Communication
7. Load Balancing
8. Retry
9. Idempotency
10. CQRS
11. Distributed Tracing
12. Rate Limiting
```

---

# 3. Pattern -> Problem Mapping

A very important interview concept is:

> **Don't memorize only the pattern. Understand the problem that the pattern solves.**

| Pattern                     | Main Problem It Solves                             |
|-----------------------------|----------------------------------------------------|
| API Gateway                 | Multiple entry points / exposing internal services |
| Service Discovery           | Finding dynamic service instances                  |
| Inter-Service Communication | Communication between microservices                |
| Aggregator                  | Multiple client calls to different services        |
| Circuit Breaker             | Cascading failures                                 |
| Retry                       | Temporary/transient failures                       |
| Timeout                     | Long-running remote calls                          |
| Bulkhead                    | Resource exhaustion caused by one dependency       |
| Database per Service        | Tight coupling through shared database             |
| Saga                        | Distributed transactions                           |
| CQRS                        | Different read/write requirements                  |
| Event Sourcing              | Maintaining complete state-change history          |
| Load Balancing              | Distributing traffic across instances              |
| Stateless Service           | Easy horizontal scaling                            |
| Idempotency                 | Duplicate request processing                       |
| Rate Limiting               | Excessive traffic                                  |
| Strangler Fig               | Gradual monolith migration                         |
| Centralized Configuration   | Configuration management across services           |
| Sidecar                     | Offloading common infrastructure functionality     |
| Ambassador                  | Managing external communication                    |
| Distributed Tracing         | Debugging requests across services                 |
| Centralized Logging         | Searching/analyzing logs across services           |

---

