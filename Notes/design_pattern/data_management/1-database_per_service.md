# Database per Service Pattern

## 1. Definition

Each microservice owns its **own database** and manages its data independently.

Other services cannot directly access its database. They communicate through APIs or messaging.

## 2. Architecture

```text
        Order Service
             |
        Order Database

        Payment Service
             |
       Payment Database

        User Service
             |
         User Database
```

Each service controls its own schema and database operations.

## 3. Example

- **Order Service:** Stores order details.
- **Payment Service:** Stores payment details.
- **User Service:** Stores user details.

If the Order Service needs payment status, it calls the Payment Service API instead of querying the Payment Database directly.

## 4. Advantages

- Independent deployment and scaling.
- Loose coupling between services.
- Each service can choose a suitable database.
- Better data ownership and isolation.

## 5. Challenges

- Cross-service joins are not directly possible.
- Distributed transactions are difficult.
- Data consistency may require eventual consistency, events, or the Saga Pattern.
- Duplicate data may be needed for efficient queries.

## 6. Key Point

**One microservice owns its data; other services access it through APIs or events, not by directly accessing its database.**