# Service Discovery Pattern

## 1. Definition

The Service Discovery Pattern allows microservices to find the network locations of other services dynamically, without hardcoding IP addresses and ports.

**Example:** The Order Service needs to communicate with the Payment Service, but Payment Service instances may change their IP addresses or scale up and down.

## 2. Why Do We Need Service Discovery?

Without service discovery:

```text
Order Service
     |
     v
Hardcoded IP: 192.168.1.10:8082
     |
     v
Payment Service
```

If the Payment Service IP changes, the Order Service configuration must be updated.

With service discovery:

```text
Order Service
     |
     v
Service Registry
     |
     v
Payment Service Instance Address
```

The Order Service can discover the current address dynamically.

## 3. Service Registry

A Service Registry stores information about available service instances, such as service names, IP addresses, ports, and sometimes health status.

Example:

| Service         | IP Address   | Port |
|-----------------|--------------|-----:|
| Order Service   | 192.168.1.11 | 8081 |
| Payment Service | 192.168.1.12 | 8082 |
| Payment Service | 192.168.1.13 | 8082 |

The registry may contain multiple instances of the same service.

## 4. Service Registration

Service Registration is the process through which a service instance registers its location with the Service Registry.

```text
Payment Service starts
         |
         v
Registers IP and port
         |
         v
Service Registry
```

Registration may happen automatically when the service starts. Depending on the technology, instances may also send heartbeats or use health checks to indicate availability.

## 5. Service Discovery

Service Discovery is the process of finding the available instances of a required service.

```text
Order Service
      |
      | Find Payment Service
      v
Service Registry
      |
      | Returns available instances
      v
Payment Service Instances
```

The caller or an intermediary then selects an instance and sends the request.

## 6. Client-Side Discovery

In client-side discovery, the client queries the registry and selects a service instance itself.

**Example:** Spring Cloud Netflix Eureka with Spring Cloud LoadBalancer.

```text
Order Service
      |
      v
Eureka Registry
      |
      v
Available Payment Instances
      |
      v
Order Service selects an instance
      |
      v
Payment Service
```

**Advantages:**
- Client can choose an instance using a load-balancing algorithm.
- No separate server-side discovery proxy is required.

**Disadvantage:**
- The client needs discovery and load-balancing logic.

## 7. Server-Side Discovery

In server-side discovery, the client sends a request to a load balancer or proxy. That component discovers or resolves the appropriate backend instance and forwards the request.

```text
Client
   |
   v
Load Balancer / Proxy
   |
   v
Service Discovery Mechanism
   |
   v
Payment Service Instance
```

**Advantages:**
- Clients do not need to implement service discovery themselves.
- Discovery and routing can be centralized.

**Example:** A Kubernetes Service routes requests to eligible backend Pods.

## 8. Eureka

Eureka is a service registry commonly used with Spring Cloud microservices.

It supports service registration and discovery.

### Example Architecture

```text
             Eureka Server
              /         \
             v           v
      Order Service   Payment Service
        (registers)     (registers)
             |
             v
      Discovers Payment
             |
             v
      Calls Payment Service
```

### Common Spring Cloud Components

- **Eureka Server:** Maintains the registry of service instances.
- **Eureka Client:** Registers an instance and retrieves service information.
- **Spring Cloud LoadBalancer:** Selects an instance for a service request.

### Important Point

Eureka is a registry, not itself a load-balancing algorithm. It provides instance information; a discovery client and load balancer can use that information to route requests.

## 9. Kubernetes Service Discovery

Kubernetes provides built-in service discovery through Services and cluster DNS.

Instead of requiring every application to know the changing IP addresses of individual Pods, clients can communicate using a stable Service name.

```text
Order Pod
    |
    | http://payment-service
    v
Kubernetes Service
    |
    +----> Payment Pod 1
    |
    +----> Payment Pod 2
    |
    +----> Payment Pod 3
```

For example:

```text
http://payment-service:8080
```

Within the same namespace, `payment-service` commonly resolves to the Kubernetes Service.

For cross-namespace communication, use a namespace-qualified name, such as:

```text
http://payment-service.payments.svc.cluster.local:8080
```

Kubernetes Service routing distributes traffic to eligible endpoints according to the configured networking implementation.

**Important:** Kubernetes usually handles service discovery through Services, DNS, and endpoint information. You do not normally need Eureka solely for discovery between services inside a Kubernetes cluster.

## 10. Eureka vs Kubernetes Service Discovery

| Eureka                                              | Kubernetes                                              |
|-----------------------------------------------------|---------------------------------------------------------|
| Commonly used with Spring Cloud applications.       | Built into the Kubernetes platform.                     |
| Uses a service registry and discovery clients.      | Uses Services, DNS, and endpoint management.            |
| Clients commonly discover instances through Eureka. | Clients commonly call a stable Service DNS name.        |
| Useful in supported Spring Cloud deployments.       | Natural choice for applications deployed on Kubernetes. |

## 11. Key Point

**Service Discovery allows microservices to find available service instances dynamically instead of relying on hardcoded IP addresses and ports.**

- Service Registry: Stores service instance information.
- Service Registration: Adds an instance to the registry.
- Service Discovery: Finds available instances.
- Client-Side Discovery: The client selects an instance.
- Server-Side Discovery: A proxy or load balancer selects or routes to an instance.
- Eureka: A service registry commonly used with Spring Cloud.
- Kubernetes: Provides service discovery through Services and DNS.