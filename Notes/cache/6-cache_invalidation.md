# Cache Invalidation in System Design

## 1. What is Cache Invalidation?

Cache Invalidation is the process of removing or updating stale data from the cache when the original data changes in the database.

**In simple words:** When data changes in the database, we need to make sure that the cache does not continue serving outdated data.

### Why do we need Cache Invalidation?

Suppose an e-commerce application stores product details in Redis.

Initially:

```text
Database:
product:101 = ₹80,000

Redis Cache:
product:101 = ₹80,000
```

Now the business updates the product price:

```text
Database:
product:101 = ₹90,000

Redis Cache:
product:101 = ₹80,000
```

Now the database and cache contain different values.

The cache contains stale data.

If the application reads from Redis, it returns the outdated price.

Cache invalidation helps solve this problem.

---

## 2. Cache Invalidation Strategies

There are three common approaches:

1. TTL-based Invalidation
2. Explicit Cache Invalidation
3. Event-Driven Cache Invalidation

---

## 3. TTL-Based Invalidation

TTL stands for Time To Live.

In this approach, we assign an expiration time to cached data.

Once the TTL expires, the cached entry becomes invalid and is no longer returned as a valid cache hit.

### Example

Suppose the application stores product details in Redis with a TTL of 5 minutes.

```text
10:00 AM -> Redis stores ₹80,000 (TTL = 5 minutes)

10:02 AM -> Database price changes to ₹90,000

10:05 AM -> Cached entry expires

Next Request -> Cache Miss

Application fetches ₹90,000 from Database
and populates Redis again.
```

### Advantages

- Simple to implement.
- Automatically expires old cache entries.
- No need to explicitly remove every cache entry after an update.

### Disadvantages

- Stale data can be served until the TTL expires.
- Very short TTL causes frequent database queries.
- Very long TTL increases the possibility of stale data.

**Important:** TTL limits the lifetime of stale data; it does not guarantee immediate consistency.

---

## 4. Explicit Cache Invalidation

In this approach, the application explicitly removes or updates the corresponding cache entry whenever the database data changes.

This is also commonly called **Cache Invalidation on Write**.

### Example

Initially:

```text
Database:
product:101 = ₹80,000

Redis:
product:101 = ₹80,000
```

Now the application receives a request to update the product price to ₹90,000.

### Workflow

1. Update the product price in the database.
2. After the database update succeeds, invalidate the corresponding cache entry.
3. The next read results in a cache miss.
4. Application fetches the latest price from the database.
5. Application stores the latest data in Redis.

### Flow

```text
Client
   |
   v
Application Server
   |
   v
Update Database
   |
   v
Database Update Successful
   |
   v
Delete Cache Entry
   |
   v
Next Read -> Cache Miss
   |
   v
Fetch Latest Data from Database
   |
   v
Populate Cache
```

### Java / Spring Boot-style Example

```java
public void updateProduct(Long productId, BigDecimal price) {

    // 1. Update database
    Product product = productRepository.findById(productId)
            .orElseThrow();

    product.setPrice(price);
    productRepository.save(product);

    // 2. Invalidate cache after DB update
    redis.delete("product:" + productId);
}
```

### Advantages

- Cache is invalidated promptly after a successful database update, assuming invalidation succeeds.
- Reduces the stale-data window compared with waiting for TTL.
- Simple and effective for many applications.

### Disadvantages

- Application must know which cache keys need invalidation.
- If the database update succeeds but cache deletion fails, stale data may remain.
- Multiple related cache keys may need to be invalidated.

**Important:** Updating the database and deleting a Redis key are two separate operations. They are not automatically part of one atomic transaction.

---

## 5. Event-Driven Cache Invalidation

In Event-Driven Cache Invalidation, the application publishes an event whenever important data changes in the database.

A separate consumer or service listens to that event and invalidates the corresponding cache entry.

This approach is especially useful in microservices architecture.

### Example

Consider an e-commerce application with two microservices:

- Product Service: Manages product details.
- Inventory Service: Manages product availability.

Suppose Product Service updates a product's price.

Other services may also have cached product information.

How will those services know that the product data has changed?

We can use an event/message broker such as Kafka or RabbitMQ.

### Workflow

1. Product Service updates the product in the database.
2. Product Service publishes a `ProductUpdated` event.
3. Kafka/RabbitMQ delivers the event to interested consumers.
4. Consumer identifies the affected cache key.
5. Consumer invalidates the corresponding cache entry.
6. The next read fetches fresh data from the database.

### Flow

```text
             Product Service
                    |
                    v
              Update Database
                    |
                    v
             Publish Event
                    |
                    v
          Kafka / RabbitMQ
                    |
                    v
       Cache Invalidation Consumer
                    |
                    v
              Redis Cache
                    |
                    v
          Delete product:101
```

### Example Event

```json
{
  "eventType": "ProductUpdated",
  "productId": 101,
  "updatedPrice": 90000
}
```

### Java-style Consumer Example

```java
public void handleProductUpdated(ProductUpdatedEvent event) {

    redis.delete("product:" + event.getProductId());
}
```

### Advantages

- Cache invalidation can be handled independently of the service updating the data.
- Multiple services can react to the same event.
- Useful in distributed and microservices architectures.
- Reduces direct dependencies between services.

### Disadvantages

- Event delivery and processing can be delayed.
- Cache may temporarily contain stale data until the event is processed.
- Events can be delivered more than once, so consumers should be idempotent.
- Failed event processing needs retries and monitoring.

### Important: Transactional Outbox Pattern

Publishing an event after a database update introduces a reliability problem.

For example:

1. Database update succeeds.
2. Application tries to publish an event to Kafka.
3. Kafka is unavailable.
4. Event is not published.

Now the database contains updated data, but other services are not notified.

A common solution is the **Transactional Outbox Pattern**.

Workflow:

1. Update business data in the database.
2. Insert an event record into an outbox table in the same database transaction.
3. A background publisher reads the outbox and sends the event to Kafka/RabbitMQ.
4. Consumer receives the event and invalidates the cache.

This helps prevent losing the event when the database update succeeds.

---

## 6. Comparison of Cache Invalidation Strategies

| Feature                 | TTL-Based                             | Explicit                   | Event-Driven        |
|-------------------------|---------------------------------------|----------------------------|---------------------|
| Trigger                 | Expiration time                       | Application update logic   | Published event     |
| Who handles it?         | Cache expiration mechanism            | Application                | Event consumer      |
| Immediate invalidation? | No                                    | Usually, if successful     | Not necessarily     |
| Complexity              | Low                                   | Medium                     | Higher              |
| Best suited for         | Data that can tolerate some staleness | Direct application updates | Distributed systems |

### Which Strategy Should We Use?

- **TTL-Based:** When occasional stale data is acceptable.
- **Explicit Invalidation:** When the application directly controls updates and needs to invalidate cache entries promptly.
- **Event-Driven:** When multiple services need to know about data changes.

These strategies can also be combined.

For example, a production application might use explicit or event-driven invalidation for freshness, with TTL as a safety mechanism if an invalidation event is missed.

---

## 7. Cache Invalidation Failure

Consider this sequence:

1. Application updates the database successfully.
2. Application tries to delete the Redis key.
3. Redis is temporarily unavailable.
4. The old cache entry remains.

Now the database and cache are inconsistent.

### Possible Solutions

- Retry cache invalidation.
- Use TTL as a fallback.
- Use event-driven invalidation.
- Use the Transactional Outbox Pattern to reliably publish update events.

**Interview Point:** Cache invalidation is difficult because updating the database and updating or deleting the cache are generally not one atomic operation.

---

## 8. Interview Questions

**Q1. What is Cache Invalidation?**

Cache Invalidation is the process of removing or updating stale cache entries when the original data changes.

**Q2. Why is Cache Invalidation required?**

Because cached data can become outdated when the database is updated. Without invalidation, the application may return stale data.

**Q3. What are the common Cache Invalidation strategies?**

1. TTL-based Invalidation
2. Explicit Cache Invalidation
3. Event-Driven Cache Invalidation

**Q4. What is the difference between TTL and Explicit Invalidation?**

- TTL invalidates cache entries after a configured expiration time.
- Explicit invalidation removes or updates cache entries when the application updates the database.

**Q5. Why is Event-Driven Invalidation useful in microservices?**

Because multiple services can subscribe to the same event and invalidate their own cached data without requiring direct communication between those services.

**Q6. What is the Transactional Outbox Pattern?**

It stores an event record in the same database transaction as the business data update. A background publisher then sends the event to a message broker, helping avoid lost events.

**Q7. Does TTL guarantee immediate cache consistency?**

No. TTL only limits how long stale data can remain available. It does not guarantee immediate consistency.

---

## 9. Quick Revision

- Cache Invalidation = Removing or updating stale cache data.
- TTL-Based = Cache entry expires after a configured time.
- Explicit Invalidation = Application removes or updates cache after a database update.
- Event-Driven = A published event triggers cache invalidation.
- Transactional Outbox = Helps reliably publish events after database changes.
- TTL can be combined with explicit or event-driven invalidation as a fallback.