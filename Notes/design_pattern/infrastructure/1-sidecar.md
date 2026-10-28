# Sidecar Pattern

## 1. Definition

The Sidecar Pattern is a microservices architectural pattern in which a helper process or container runs alongside the main application to provide supporting functionality.

The sidecar handles common infrastructure tasks without requiring the main application to implement them directly.

## 2. Architecture

```text
       Same Pod / Deployment Unit
    +--------------------------------+
    |                                |
    |   Main Application Container   |
    |   (Spring Boot Service)        |
    |                                |
    |   Sidecar Container            |
    |   - Logging                    |
    |   - Networking                 |
    |   - Security                   |
    |                                |
    +--------------------------------+
```

In Kubernetes, the main application and sidecar commonly run as separate containers in the same Pod and share the Pod's network namespace.

## 3. Example

Suppose you have a Spring Boot Order Service.

Without a sidecar, the application may need additional code or libraries for tasks such as collecting logs or managing certain network behaviors.

With a sidecar, these responsibilities can be handled by a separate helper container.

```text
Order Service
     |
     +----> Sidecar Proxy
                  |
                  v
            Payment Service
```

The application can communicate through the proxy, while the sidecar handles supported networking concerns.

## 4. Common Use Cases

### 4.1 Logging

A sidecar collects application logs and forwards them to a centralized logging system.

**Example:** Fluent Bit collecting logs and forwarding them to a log platform.

### 4.2 Networking

A sidecar proxy manages service-to-service traffic, routing, timeouts, or telemetry.

**Example:** Envoy proxy.

### 4.3 Security

A sidecar can help with supported security tasks, such as TLS termination, certificate handling, or identity-based communication.

Security capabilities depend on its configuration and deployment.

### 4.4 Service Mesh

A service mesh uses infrastructure components to manage communication between services.

Sidecar proxies have traditionally been a common implementation approach.

## 5. Sidecar Pattern in Kubernetes

Example Pod configuration:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: order-service
spec:
  containers:
    - name: application
      image: order-service:1.0
      ports:
        - containerPort: 8080

    - name: sidecar
      image: fluent/fluent-bit:latest
```

This illustrates two containers in one Pod. A real logging sidecar also needs appropriate log access and configuration; the example alone does not configure log collection.

## 6. Advantages

- Separates infrastructure concerns from business logic.
- Allows supporting components to be managed independently.
- Enables reuse of common networking, logging, and security capabilities.
- Can reduce duplicated infrastructure code across services.

## 7. Challenges

- Consumes additional CPU and memory.
- Adds deployment and operational complexity.
- Sidecar configuration and version compatibility must be managed.
- A sidecar failure can affect the functionality it provides.

## 8. Key Point

**The Sidecar Pattern places supporting functionality alongside the main application, allowing infrastructure concerns such as logging, networking, and security to be handled separately from business logic.**