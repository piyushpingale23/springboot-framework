# Aggregator Pattern

## 1. Definition

The Aggregator Pattern combines data from multiple microservices into a single response.

Instead of the client calling multiple services independently, it calls one endpoint that coordinates the required service calls and combines their responses.

## 2. Architecture

```text
                 Client
                   |
                   v
            Aggregator Service
              /     |     \
             v      v      v
        User      Order   Payment
        Service   Service  Service
             \      |      /
              \     |     /
                   v
           Combined Response
                   |
                   v
                 Client
```

## 3. Example: E-Commerce Order Details

Suppose the client needs to display an order details page containing:

- Customer information
- Order information
- Payment status

Without the Aggregator Pattern, the client calls three services separately.

```text
Client --> User Service
Client --> Order Service
Client --> Payment Service
```

With the Aggregator Pattern:

```text
Client
   |
   v
Order Aggregator
   |
   +----> User Service
   |
   +----> Order Service
   |
   +----> Payment Service
   |
   v
Combined JSON Response
```

## 4. How to Implement It in Spring Boot

Assume three microservices expose these endpoints:

```text
User Service:
GET http://localhost:8081/users/10

Order Service:
GET http://localhost:8082/orders/101

Payment Service:
GET http://localhost:8083/payments/101
```

The Aggregator Service calls these endpoints, combines their responses, and returns a single JSON object.

### Step 1: Create Response DTOs

```java
public record UserResponse(
        Long id,
        String name
) {}

public record OrderResponse(
        Long id,
        Long userId,
        double amount
) {}

public record PaymentResponse(
        Long orderId,
        String status
) {}
```

### Step 2: Create the Aggregated Response

```java
public record OrderDetailsResponse(
        UserResponse user,
        OrderResponse order,
        PaymentResponse payment
) {}
```

### Step 3: Implement the Aggregator Service

Using Spring's `RestClient`:

```java
@Service
public class OrderAggregatorService {

    private final RestClient restClient;

    public OrderAggregatorService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public OrderDetailsResponse getOrderDetails(
            Long userId, Long orderId) {

        UserResponse user = restClient.get()
                .uri("http://localhost:8081/users/{id}", userId)
                .retrieve()
                .body(UserResponse.class);

        OrderResponse order = restClient.get()
                .uri("http://localhost:8082/orders/{id}", orderId)
                .retrieve()
                .body(OrderResponse.class);

        PaymentResponse payment = restClient.get()
                .uri("http://localhost:8083/payments/{id}", orderId)
                .retrieve()
                .body(PaymentResponse.class);

        return new OrderDetailsResponse(user, order, payment);
    }
}
```

### Step 4: Create the Controller

```java
@RestController
@RequestMapping("/api/order-details")
public class OrderAggregatorController {

    private final OrderAggregatorService aggregatorService;

    public OrderAggregatorController(
            OrderAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @GetMapping
    public OrderDetailsResponse getOrderDetails(
            @RequestParam Long userId,
            @RequestParam Long orderId) {

        return aggregatorService.getOrderDetails(userId, orderId);
    }
}
```

### Step 5: Call the Aggregator Endpoint

```http
GET http://localhost:8080/api/order-details?userId=10&orderId=101
```

Example response:

```json
{
  "user": {
    "id": 10,
    "name": "Rahul"
  },
  "order": {
    "id": 101,
    "userId": 10,
    "amount": 1500.0
  },
  "payment": {
    "orderId": 101,
    "status": "SUCCESS"
  }
}
```

The client makes **one API call** and receives the combined response.

*This is a basic sequential example. In production, configure timeouts, error handling, service discovery, and authentication. Independent service calls can also run concurrently to reduce latency.*

## 5. API Composition

API Composition means retrieving data from multiple services and combining it into one response.

The Aggregator Pattern is a common way to implement API Composition.

Example:

```text
Product Service  ----\
Inventory Service ----> Product Catalog Aggregator
Price Service    ----/            |
                                   v
                          Combined Product Response
```

The aggregator combines product details, stock availability, and pricing.

## 6. Client-Specific Responses

Different clients may require different response formats or data.

Example:

- **Mobile App:** Product name, price, and stock status.
- **Web App:** Product details, price, stock, and reviews.
- **Admin Dashboard:** Product details, stock, sales, and internal metrics.

An aggregator or a dedicated backend-for-frontend (BFF) can return the data tailored to each client.

## 7. Advantages

- Reduces the number of client API calls.
- Hides the complexity of communicating with multiple services.
- Combines related information into a single response.
- Supports client-specific response formats.

## 8. Challenges

- The aggregator can become a bottleneck or single point of failure.
- Slow downstream services can increase response time.
- Failure handling becomes more complex when one service is unavailable.
- Large aggregators can become difficult to maintain.

## 9. Aggregator vs API Gateway

| Aggregator Pattern                                             | API Gateway                                                                                |
|----------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| Focuses on calling multiple services and combining their data. | Focuses on providing a unified entry point to backend services.                            |
| Contains aggregation or composition logic.                     | Commonly handles routing, authentication, rate limiting, and other cross-cutting concerns. |
| Example: Combine user, order, and payment details.             | Example: Route `/orders` to Order Service.                                                 |

An API Gateway can also perform aggregation, but the two patterns describe different responsibilities.

## 10. Key Point

**The Aggregator Pattern calls multiple microservices, combines their responses, and returns a single response to the client.**