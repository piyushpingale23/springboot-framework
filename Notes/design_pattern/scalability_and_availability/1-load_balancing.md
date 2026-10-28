# Load Balancing Pattern

## 1. Definition

The Load Balancing Pattern distributes incoming requests across multiple servers or service instances to prevent overloading a single instance.

A load balancer acts as an entry point between clients and backend servers.

## 2. Architecture

```text
                 Clients
                    |
                    v
              Load Balancer
               /    |    \
              v     v     v
          Server1 Server2 Server3
              |     |     |
              +-----+-----+
                    |
                 Database
```

## 3. Why Do We Need Load Balancing?

Suppose a Spring Boot application runs on a single instance and receives 10,000 requests.

If that instance cannot handle the traffic, response time increases and requests may fail.

Instead, run multiple instances and distribute requests among them.

**Benefits:**
- Improves scalability and availability.
- Distributes traffic across instances.
- Prevents a single instance from becoming a bottleneck.
- Supports traffic routing away from unhealthy instances when health checks are configured.

## 4. Types of Load Balancing

### 4.1 Client-Side Load Balancing

The client selects a backend instance from available instances.

**Example:** Spring Cloud LoadBalancer.

```text
Client / Service
       |
       +----> Instance 1
       +----> Instance 2
       +----> Instance 3
```

### 4.2 Server-Side Load Balancing

A separate load balancer receives requests and forwards them to backend instances.

**Examples:** NGINX, HAProxy, AWS Elastic Load Balancing.

```text
Client
   |
   v
NGINX Load Balancer
   |
   +----> Spring Boot Instance 1
   +----> Spring Boot Instance 2
   +----> Spring Boot Instance 3
```

## 5. Common Load Balancing Algorithms

- **Round Robin:** Sends requests to instances sequentially.
- **Weighted Round Robin:** Sends more requests to instances with higher weights.
- **Least Connections:** Selects the instance with the fewest active connections.
- **IP Hash:** Uses the client's IP address to select an instance.

## 6. NGINX as a Load Balancer

NGINX is a web server that can also work as a reverse proxy and load balancer.

It receives client requests and forwards them to backend application instances.

### Example Architecture

Suppose three Spring Boot instances run on different ports:

```text
Client
  |
  v
NGINX (Port 80)
  |
  +----> Spring Boot Instance 1 (Port 8081)
  |
  +----> Spring Boot Instance 2 (Port 8082)
  |
  +----> Spring Boot Instance 3 (Port 8083)
```

Clients send requests to NGINX instead of calling each Spring Boot instance directly.

### NGINX Configuration

File: `/etc/nginx/conf.d/spring-app.conf`

```nginx
upstream spring_backend {
    # Default algorithm: Round Robin
    server 127.0.0.1:8081;
    server 127.0.0.1:8082;
    server 127.0.0.1:8083;
}

server {
    listen 80;

    location / {
        proxy_pass http://spring_backend;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**How it works:**

1. The client sends a request to NGINX on port `80`.
2. NGINX receives the request.
3. NGINX selects a backend instance using Round Robin by default.
4. The selected Spring Boot instance processes the request.
5. NGINX forwards the response to the client.

### Testing

```bash
sudo nginx -t
sudo systemctl reload nginx
```

If the configuration is valid and the backend applications are running, send requests to:

```text
http://localhost/
```

Repeated requests can be distributed across the configured instances.

## 7. Weighted Load Balancing in NGINX

If one instance has more resources than the others, assign it a higher weight.

```nginx
upstream spring_backend {
    server 127.0.0.1:8081 weight=3;
    server 127.0.0.1:8082 weight=1;
}
```

NGINX distributes requests approximately in a 3:1 ratio under suitable conditions.

- Instance 1 receives roughly 75% of requests.
- Instance 2 receives roughly 25% of requests.

Actual traffic can vary depending on connection behavior and request patterns.

## 8. Health Checks and Failures

If a backend becomes unavailable, NGINX can detect certain connection failures and temporarily avoid sending requests to that server.

```nginx
upstream spring_backend {
    server 127.0.0.1:8081 max_fails=3 fail_timeout=10s;
    server 127.0.0.1:8082;
    server 127.0.0.1:8083;
}
```

Here, `max_fails` and `fail_timeout` configure passive failure detection. They do not provide continuous active health checks.

**Important:** Basic NGINX Open Source configurations do not provide the same active health-check functionality as NGINX Plus.

## 9. Important Considerations

- **Session management:** Prefer stateless applications or shared session storage so requests can reach different instances.
- **Database:** Multiple application instances can connect to the same database, but the database may become the next bottleneck.
- **Single point of failure:** A single NGINX instance can itself fail; production deployments may require redundant load balancers.
- **HTTPS:** NGINX can terminate TLS and forward requests to backend instances.
- **Monitoring:** Monitor request rates, response times, errors, and backend health.

## 10. Load Balancer vs API Gateway

| Load Balancer                                     | API Gateway                                                                    |
|---------------------------------------------------|--------------------------------------------------------------------------------|
| Distributes traffic across instances.             | Provides a unified API entry point for clients.                                |
| Focuses on availability and traffic distribution. | Can handle authentication, routing, rate limiting, and request transformation. |
| Example: NGINX configured for load balancing.     | Example: Spring Cloud Gateway.                                                 |

NGINX can also perform reverse proxy and API-gateway-like functions, depending on its configuration and available features.

## 11. Quick Summary

- **Load Balancing:** Distributes requests across multiple instances.
- **NGINX:** Can act as a reverse proxy and server-side load balancer.
- **Round Robin:** Default NGINX upstream algorithm.
- **Weighted Load Balancing:** Distributes traffic according to configured weights.
- **High Availability:** Requires handling both backend failures and load-balancer failures.

**Key Point:** Load balancing allows a Spring Boot application to scale horizontally by running multiple instances and distributing incoming traffic across them.