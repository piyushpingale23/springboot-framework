# API Gateway Pattern

## 1.1 Definition

API Gateway is a **single entry point** between clients and backend microservices.

Instead of clients directly communicating with multiple microservices, requests first go through the API Gateway.

---

## 1.2 Without API Gateway

```text
                         ┌──> User Service
                         │
Client ──────────────────┼──> Order Service
                         │
                         ├──> Payment Service
```

### Problems

* Client needs to know every microservice.
* Internal service URLs are exposed.
* Client becomes coupled to internal architecture.

---

# 2. With API Gateway

```text
                         ┌──────────────────┐
                         │   API Gateway    │
                         └────────┬─────────┘
                                  |
                    ┌─────────────┼─────────────┐
                    ↓             ↓             ↓
              User Service   Order Service   Payment Service
```

The client only communicates with the Gateway.

```text
Client
   |
   | GET /orders/100
   ↓
API Gateway
   |
   ↓
Order Service
```

---

# 3. Main Problems Solved by API Gateway

API Gateway provides a centralized place for:

```text
API Gateway
     |
     ├── SSL/TLS Termination
     ├── Authentication
     ├── Authorization
     ├── Rate Limiting
     ├── Request Aggregation
     ├── Routing
     ├── Logging
     └── Load Balancing Integration
```

### Main objective

> Hide the internal microservice architecture from clients and provide centralized API-level management.

---

# 4. SSL/TLS Termination

## Definition

SSL/TLS termination means the Gateway handles the external HTTPS connection and performs TLS encryption/decryption.

SSL/TLS termination reduces the complexity of managing HTTPS certificates across multiple backend services by handling TLS at the API Gateway.

```text
Client
   |
   | HTTPS
   ↓
API Gateway
   |
   | HTTP / HTTPS
   ↓
Microservice
```

---

# 5. Authentication

## Definition

Authentication answers:

> **Who are you?**

It verifies the identity of the client/user.

Common mechanisms:

* JWT
* OAuth 2.0
* OpenID Connect
* API Keys
* Username/password

### Example

---

# 6. Authorization

## Definition

Authorization answers:

> **What are you allowed to do?**

---

# 7. Rate Limiting

## Definition

Rate limiting controls **how many requests a client can make within a specific time period**.

### Problem

```text
Backend capacity = 1,000 requests/sec

Client sends = 50,000 requests/sec
```

This can overload the system.

### Architecture

```text
Client
   |
   ↓
API Gateway
   |
   ↓
Rate Limiter
   |
   ├── Allowed → Backend
   |
   └── Rejected → 49000
```

---

# 8. Routing

## Definition

Routing means **forwarding an incoming request to the appropriate backend microservice**.

---

# 9. Request Aggregation

## Definition

Request aggregation means the Gateway calls multiple services and combines their responses into a single response.

### Without Aggregation

```text
Mobile App
   |
   ├──> User Service
   ├──> Order Service
   ├──> Payment Service
   └──> Product Service
```

### With Aggregation

```text
                    Mobile App
                        |
                        ↓
                 API Gateway
                  Aggregator
                /      |      \
               ↓       ↓       ↓
            User     Order   Payment
            Service  Service  Service
               \       |       /
                \      |      /
                ↓      ↓      ↓
                 Combined Response
```

---

# 10. Logging

The Gateway can log incoming requests and responses.

Example:

```text
timestamp=2026-10-07
requestId=ABC123
method=GET
path=/orders/100
status=200
latency=120ms
```

---

# 11. Load Balancing Integration

Suppose we have multiple instances:

```text
Order Service
    |
    ├── Instance 1
    ├── Instance 2
    └── Instance 3
```

Gateway/load balancer distributes requests:

```text
                  API Gateway
                       |
                Load Balancing
               /      |      \
              ↓       ↓      ↓
         Order-1   Order-2   Order-3
```

Example:

```text
Request 1 → Order-1
Request 2 → Order-2
Request 3 → Order-3
Request 4 → Order-1
```

### Benefits

* Horizontal scaling
* High availability
* Better traffic distribution
* Failure handling

---

# 12. Complete API Gateway Request Flow

Example request:

```http
GET /api/orders/100
Authorization: Bearer <JWT>
```

Flow:

```text
                    CLIENT
                      |
                      | HTTPS
                      ↓
              ┌───────────────────┐
              │    API GATEWAY    │
              └─────────┬─────────┘
                        |
                        ↓
                 SSL TERMINATION
                        |
                        ↓
                 Authentication
                        |
                   JWT valid?
                    /      \
                  NO        YES
                  ↓          ↓
                401      Authorization
                              |
                         Role allowed?
                          /       \
                        NO         YES
                        ↓           ↓
                      403       Rate Limit
                                    |
                               Allowed?
                               /      \
                             NO        YES
                             ↓          ↓
                           429       Routing
                                        |
                                        ↓
                                Service Discovery
                                        |
                                        ↓
                                 Load Balancing
                                        |
                                  ┌─────┼─────┐
                                  ↓     ↓     ↓
                                Order Order Order
                                 #1    #2    #3
```

---

# 14. API Gateway vs Load Balancer

## Load Balancer

Main responsibility:

> Distribute traffic among multiple instances.

```text
                Load Balancer
                 /    |    \
                ↓     ↓    ↓
              App   App   App
```

## API Gateway

Main responsibility:

> Provide a controlled entry point to backend APIs.

```text
                 API Gateway
                      |
       ┌──────────────┼──────────────┐
       ↓              ↓              ↓
     User           Order          Payment
```

API Gateway can provide:

* Routing
* Authentication
* Authorization
* Rate limiting
* Aggregation
* SSL termination
* Logging
* Load balancing integration

---

# 15. API Gateway High Availability

Do not deploy only one Gateway instance.

### Bad

```text
Client
  |
    ↓
Gateway
  |
     ↓
All Services
```

If Gateway goes down, clients cannot reach services.

### Better

```text
                  Load Balancer
                  /           \
                                   ↓                         ↓
          API Gateway-1   API Gateway-2
                 |             |
                 └──────┬──────┘
                                                ↓
                 Backend Services
```

---

# 16. API Gateway Should Not Contain Business Logic

### Bad

```text
API Gateway
     |
     ├── Calculate payment
     ├── Calculate discount
     ├── Update inventory
     └── Business rules
```

### Good

```text
API Gateway
     |
     ├── Routing
     ├── Authentication
     ├── Authorization
     ├── Rate Limiting
     ├── Aggregation
     └── Observability
              |
              ↓
       Business Services
              |
       ┌──────┼──────┐
       ↓      ↓      ↓
     Order Payment Inventory
```

Business logic should remain inside the appropriate microservice.

---

# 17. Advantages

1. Single entry point
2. Hides internal architecture
3. Centralized authentication
4. Centralized authorization
5. Rate limiting
6. Request aggregation
7. SSL/TLS management
8. Centralized logging
9. Routing
10. Load balancing integration
11. API-level monitoring
12. Request/response transformation

---

# 18. Disadvantages

### 1. Single Point of Failure

If only one Gateway exists:

```text
Client
  |
  X
Gateway DOWN
```

Solution:

```text
Load Balancer
    |
    ├── Gateway-1
    └── Gateway-2
```

### 2. Additional Network Hop

```text
Client → Gateway → Service
```

### 3. Possible Bottleneck

Gateway needs to be scalable and highly available.

---

# 19. Common Questions

### Q1. What is API Gateway?

> API Gateway is a single entry point for clients to access multiple microservices. It handles request routing and can centralize concerns such as authentication, authorization, rate limiting, SSL termination, request aggregation, logging, and load balancing integration.

### Q2. Why do we need API Gateway?

> It hides the internal microservice architecture from clients and provides centralized API management, security, routing, traffic control, and observability.

### Q3. What is Routing?

> Routing means forwarding a request to the appropriate backend microservice based on request properties such as path, method, headers, or host.

### Q4. Authentication vs Authorization?

> Authentication verifies who the user is, while authorization determines what that authenticated user is allowed to access.

### Q5. What is Rate Limiting?

> Rate limiting controls how many requests a client can make within a specified time period to protect backend services from excessive traffic.

### Q6. What is Request Aggregation?

> Request aggregation allows the Gateway to call multiple backend services and combine their responses into a single response for the client.

### Q7. What is SSL Termination?

> SSL/TLS termination means the Gateway handles the external HTTPS connection and performs TLS encryption/decryption.

### Q8. Why is Logging useful at Gateway?

> Because requests pass through the Gateway, it provides a centralized point for access logging, request IDs, response status, and latency information.

### Q9. How does Gateway perform load balancing?

> The Gateway can integrate with service discovery and load balancing to distribute requests among multiple instances of a backend service.

### Q10. Should API Gateway contain business logic?

> No. The Gateway should generally handle API-level concerns such as routing, security, rate limiting, aggregation, and observability. Business logic should remain in the appropriate microservice.

---

# 20. Final Definition

> **API Gateway is the front door of a microservices' architecture. It acts as a single entry point for clients, routes requests to appropriate services, and can handle cross-cutting concerns such as authentication, authorization, rate limiting, SSL termination, logging, request aggregation, and load balancing integration. Its main goal is to hide internal service topology from clients and provide centralized API management.**

---
