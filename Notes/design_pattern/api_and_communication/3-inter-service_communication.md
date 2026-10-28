# Inter-Service Communication Pattern

## 1. Definition

Inter-Service Communication is the way microservices communicate and exchange data with each other.

There are two main approaches:

1. **Synchronous Communication:** The caller waits for a response.
2. **Asynchronous Communication:** The caller sends a message or event and continues without waiting for the consumer to finish processing it.

## 2. Synchronous Communication

In synchronous communication, one service sends a request to another service and waits for its response.

```text
Order Service
     |
     | Request
     v
Payment Service
     |
     | Response
     v
Order Service continues
```

### 2.1 REST

REST commonly uses HTTP methods such as `GET`, `POST`, `PUT`, and `DELETE`, typically with JSON payloads.

Example:

```http
POST /payments
Content-Type: application/json
```

```json
{
  "orderId": 101,
  "amount": 500
}
```

**Advantages:**
- Simple and widely supported.
- Easy to test using tools such as Postman.
- Suitable for request-response APIs.

**Disadvantages:**
- The caller usually waits for the response.
- Slow or unavailable dependencies can affect the caller.

**Example:** Order Service calls Payment Service to process a payment and waits for the result.

### 2.2 gRPC

gRPC is a high-performance Remote Procedure Call (RPC) framework that commonly uses HTTP/2 and Protocol Buffers.

Example:

```text
Order Service
     |
     | gRPC call
     v
Payment Service
     |
     | gRPC response
     v
Order Service
```

**Advantages:**
- Efficient binary serialization.
- Strongly typed service contracts.
- Supports unary and streaming communication.

**Disadvantages:**
- Requires compatible client/server tooling.
- Less convenient to inspect manually than ordinary JSON APIs.

**Example:** Internal microservices communicate with low-latency, strongly typed APIs.

## 3. Asynchronous Communication

In asynchronous communication, a service sends a message or event to a broker. The consumer processes it independently.

```text
Order Service
     |
     | Publish event
     v
Message Broker
     |
     | Deliver event
     v
Notification Service
```

The Order Service does not need to wait for the Notification Service to finish processing.

### 3.1 Kafka

Apache Kafka is a distributed event-streaming platform that stores events in topics and allows consumers to process them independently.

```text
Order Service
     |
     | Publish OrderCreated
     v
Kafka Topic
     |
     +------> Notification Service
     |
     +------> Analytics Service
```

**Advantages:**
- High-throughput event processing.
- Events can be retained and replayed according to topic configuration.
- Multiple consumer groups can process the same event stream independently.

**Disadvantages:**
- Requires broker and consumer management.
- Ordering and delivery behavior depend on partitioning and configuration.

**Example:** An order-created event triggers notifications, analytics, and other downstream workflows.

### 3.2 RabbitMQ

RabbitMQ is a message broker commonly used to route messages through exchanges and queues to consumers.

```text
Order Service
     |
     | Publish message
     v
RabbitMQ Exchange
     |
     v
Queue
     |
     v
Notification Service
```

**Advantages:**
- Flexible message routing.
- Supports acknowledgments and message redelivery.
- Useful for task queues and background processing.

**Disadvantages:**
- Requires broker operations and queue management.
- Throughput and delivery behavior depend on configuration and workload.

**Example:** An order service queues an email task for a background worker.

## 4. Synchronous vs Asynchronous Communication

| Aspect              | Synchronous                              | Asynchronous                                 |
|---------------------|------------------------------------------|----------------------------------------------|
| Waiting             | Caller waits for a response              | Caller can continue after sending            |
| Common technologies | REST, gRPC                               | Kafka, RabbitMQ                              |
| Coupling            | More runtime dependency between services | Services can be more independent             |
| Response            | Immediate response is possible           | Result may arrive later                      |
| Failure handling    | Timeouts, retries, circuit breakers      | Retries, acknowledgments, dead-letter queues |
| Consistency         | Often used for immediate decisions       | Often supports eventual consistency          |
| Best suited for     | Queries and immediate results            | Events, background jobs, workflows           |

## 5. When Should You Use Each?

### Use Synchronous Communication When:

- The caller needs an immediate response.
- A user-facing request depends on the result.
- You need to retrieve data from another service.
- The operation must make a decision before continuing.

**Example:** Fetching a customer's profile or checking payment authorization.

### Use Asynchronous Communication When:

- The caller does not need an immediate result.
- A task can run in the background.
- Multiple services need to react to the same event.
- You need buffering between producers and consumers.
- Temporary consumer outages should not necessarily block the producer.

**Example:** Sending email notifications after an order is created.

## 6. Important Considerations

- **Timeouts:** Limit how long synchronous calls can wait.
- **Retries:** Retry transient failures carefully; avoid duplicate side effects.
- **Idempotency:** Make repeated requests or messages safe to process where required.
- **Message durability:** Configure persistence and acknowledgments according to business requirements.
- **Duplicate messages:** Consumers should account for possible redelivery.
- **Eventual consistency:** Asynchronous workflows may take time to update all services.
- **Observability:** Use correlation IDs and distributed tracing to follow requests across services.

## 7. Key Point

- **REST:** HTTP-based APIs, commonly using JSON.
- **gRPC:** Efficient, strongly typed remote procedure calls.
- **Kafka:** Distributed event streaming and event-driven communication.
- **RabbitMQ:** Message routing and queue-based communication.

**Remember:** Use synchronous communication when you need an answer now; use asynchronous communication when work can happen independently.