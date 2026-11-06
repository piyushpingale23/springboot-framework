# Cache Write Strategies in System Design

## 1. What are Cache Write Strategies?

Cache write strategies define how an application handles data when a user creates or updates information.

When a user updates data, we need to decide:

- Should we update the cache first?
- Should we update the database first?
- Should we update both together?
- Should we return success immediately and update the database later?

These decisions are handled by cache write strategies.

### Types of Cache Write Strategies

1. Write-Through Cache
2. Write-Back Cache (Write-Behind Cache)
3. Write-Around Cache

---

# 2. Write-Through Cache

Write-Through is a synchronous write strategy in which the application coordinates updates to both the cache and database before returning a successful response to the client.

**Key Idea:** Write to the cache and database before returning a successful response.

### Example

Suppose an e-commerce application has the following product:

| Storage     | Product Price |
|-------------|---------------|
| Database    | ₹80,000       |
| Redis Cache | ₹80,000       |

Now the user updates the product price:

```text
Old Price = ₹80,000
New Price = ₹90,000
```

### How does Write-Through work?

**Step 1: Client sends an update request.**

```http
PUT /products/101
```

Request body:

```json
{
  "price": 90000
}
```

**Step 2: Application Server writes to Cache first.**

There are two possibilities:

- Cache write fails: Return an error response. Database is not updated.
- Cache write succeeds: Continue to the database update.

**Step 3: Application Server writes to Database.**

Again, there are two possibilities:

- Database write fails: Remove the updated cache entry and return an error response.
- Database write succeeds: Both cache and database contain the updated value. Return success.

### Architecture

```text
              CLIENT
                 |
                 v
          APPLICATION SERVER
                 |
                 v
             REDIS CACHE
                 |
           Cache Write
                 |
          +------+------+
          |             |
        FAIL          SUCCESS
          |             |
          v             v
      Error Response  DATABASE
                         |
                    DB Write
                         |
                  +------+------+
                  |             |
                FAIL          SUCCESS
                  |             |
                  v             v
             Delete Cache   Return Success
             Return Error
```

### What happens if Database Write Fails?

Initially:

```text
Database = ₹80,000
Cache    = ₹80,000
```

After updating the cache:

```text
Database = ₹80,000
Cache    = ₹90,000
```

Now the database update fails.

The application removes the updated cache entry.

```text
Database = ₹80,000
Cache    = Entry removed
```

The next read request results in a cache miss, and the application fetches the original price from the database.

### Advantages

1. Cache and database are updated synchronously during the normal successful flow.
2. Reduces the possibility of stale cache data after successful writes.
3. Subsequent reads can get the latest cached data.
4. Useful when read consistency is important.

### Disadvantages

1. Write operations become slower because two storage systems must be updated.
2. Application server must coordinate both writes.
3. If one write succeeds and the other fails, recovery logic is required.
4. Additional network calls are required.

### Important Interview Point

Does Write-Through guarantee that cache and database can never go out of sync?

**No.**

It reduces inconsistency during normal successful execution, but it does not provide an atomic transaction across Redis and PostgreSQL.

For example:

- Redis update succeeds.
- Database update succeeds.
- Application crashes before returning the response.

Or the database update succeeds but a cache rollback operation fails.

Production implementations need failure handling, retries, reconciliation, and sometimes versioning.

---

# 3. Write-Back Cache (Write-Behind Cache)

Write-Back is an asynchronous write strategy in which the application first writes data into the cache and immediately returns a success response to the client.

The database update happens asynchronously in the background.

**Key Idea:** Write to the cache first, acknowledge the client, and update the database later.

### Example: YouTube Like Counter

Suppose a video currently has:

```text
Total Likes = 10,000
```

A user likes the video.

The application needs to increment the like count:

```text
Total Likes = 10,001
```

Instead of synchronously updating the database for every like, we can use Write-Back caching.

### How does Write-Back work?

**Step 1: Client sends a request.**

```http
POST /videos/101/like
```

**Step 2: Application updates the cache.**

```text
Redis:
video:101:likes = 10,001
```

**Step 3: Return success immediately.**

The application returns success without waiting for the database update.

**Step 4: Update the database asynchronously.**

A background worker processes the pending update and persists it into the database.

```text
Database:
video:101:likes = 10,001
```

### Architecture

```text
              CLIENT
                 |
                 v
          APPLICATION SERVER
                 |
                 v
             REDIS CACHE
                 |
                 v
          Return Success
                 |
                 |
           BACKGROUND
           PROCESSING
                 |
                 v
           BACKGROUND WORKER
                 |
                 v
              DATABASE
```

### Why is Write-Back faster?

**Write-Through:**

```text
Client
  |
  v
Application
  |
  v
Redis Write
  |
  v
Database Write
  |
  v
Success Response
```

The application waits for both writes.

**Write-Back:**

```text
Client
  |
  v
Application
  |
  v
Redis Write
  |
  v
Success Response

     Background:
          |
          v
       Worker
          |
          v
       Database
```

The application doesn't wait for the database write.

Therefore, response latency is reduced.

### Major Problem: Data Loss

Suppose:

1. User likes a video.
2. Redis updates the like count to 10,001.
3. Application returns success.
4. Database has not yet been updated.
5. Redis crashes before the pending write is persisted.

Now:

```text
Redis = Lost
Database = 10,000
```

The user's like was acknowledged, but the database never received the update.

This is the biggest disadvantage of Write-Back caching.

### How can we reduce the risk of data loss?

In production systems, we can use:

- Durable write queues.
- Redis persistence.
- Replication and fail over.
- Background workers with retries.
- Acknowledgment after durable recording of the pending operation.
- Idempotent database updates.

For example, we can record pending updates in a durable message queue such as Kafka.

```text
Client
  |
  v
Application
  |
  +-------> Redis Cache
  |
  +-------> Durable Event / Write Queue
                    |
                    v
              Background Worker
                    |
                    v
                 Database
```

This improves reliability, but still requires handling retries, duplicate events, ordering, and failures.

### Where can Write-Back be useful?

- View counters.
- Like counters.
- Analytics counters.
- Metrics collection.
- Certain activity counters.

It is useful when high write throughput and low response latency are important, and temporary database lag is acceptable.

**Important:** Facebook and YouTube may use distributed counters, batching, event streams, and asynchronous persistence. Their architectures are not necessarily simple Redis Write-Back implementations.

### Advantages

1. Lower write response latency.
2. Reduces synchronous database write load.
3. Supports high write throughput.
4. Allows batching of database updates.

### Disadvantages

1. Risk of data loss if pending writes are not durably recorded.
2. Database may temporarily contain stale data.
3. Requires background processing.
4. Requires retry and failure-handling mechanisms.
5. More complex implementation.

---

# 4. Write-Around Cache

Write-Around is a database-first write strategy in which the application writes directly to the database without updating the cache.

The cache is bypassed during write operations.

When data is requested later, the application fetches it from the database and can populate the cache.

**Key Idea:** Write directly to the database and bypass the cache during writes.

### Why do we need Write-Around?

Consider an application where users upload transaction records.

Suppose the application receives thousands of new transactions.

Most of these transactions may never be read again immediately.

If we cache every newly created transaction, we may fill the cache with data that is rarely accessed.

Instead, we can write directly to the database and cache the data only when it is actually requested.

### How does Write-Around work?

Suppose the application creates a new product:

```text
Product ID = 101
Product Name = iPhone
Price = ₹90,000
```

**Step 1: Client sends a request.**

```http
POST /products
```

**Step 2: Application writes directly to the database.**

```text
Database:
Product 101 = iPhone, ₹90,000
```

Redis is not updated.

**Step 3: Return success response.**

The application returns success after the database write completes.

**Step 4: User requests product 101.**

```http
GET /products/101
```

**Step 5: Check the cache.**

The product is not available in Redis.

This is a Cache Miss.

**Step 6: Fetch from database.**

The application fetches product 101 from PostgreSQL.

**Step 7: Populate the cache.**

The application stores the product in Redis.

Now subsequent requests can be served from the cache.

### Architecture

**Write Operation:**

```text
              CLIENT
                 |
                 v
          APPLICATION SERVER
                 |
                 v
              DATABASE
                 |
                 v
          Return Success
```

**Later Read Operation:**

```text
          APPLICATION SERVER
                 |
                 v
             REDIS CACHE
                 |
             Cache Miss
                 |
                 v
              DATABASE
                 |
                 v
           Fetch Data
                 |
                 v
          Populate Cache
```

### What is the role of TTL?

TTL (Time To Live) defines how long cached data remains valid.

Suppose we cache a product for 30 minutes.

```text
Product 101
TTL = 30 minutes
```

After 30 minutes, the cache entry expires.

The next request results in a Cache Miss, and the application fetches fresh data from the database.

**Important:** TTL alone does not guarantee immediate consistency.

Suppose product 101 is already cached, and someone updates its price directly in the database.

The old value may remain in Redis until its TTL expires.

Therefore, Write-Around implementations commonly invalidate the existing cache entry when updating the database, while still bypassing the cache for the actual write.

### Advantages

1. Avoids caching data that may never be read.
2. Reduces unnecessary cache writes.
3. Reduces cache memory consumption.
4. Suitable for write-heavy applications with relatively fewer reads.

### Disadvantages

1. First read after write may be slower because it results in a Cache Miss.
2. If an existing cache entry is not invalidated, stale data may be returned.
3. Frequently written and frequently read data may generate repeated database queries.

---

# 5. Comparison of All Three Write Strategies

Suppose the user updates the product price from ₹80,000 to ₹90,000.

| Parameter            | Write-Through                                | Write-Back              | Write-Around                               |
|----------------------|----------------------------------------------|-------------------------|--------------------------------------------|
| Write to cache       | Yes                                          | Yes                     | No                                         |
| Write to database    | Yes                                          | Yes, asynchronously     | Yes, directly                              |
| Database update      | Synchronous                                  | Asynchronous            | Synchronous                                |
| Client waits for DB? | Yes                                          | No                      | Yes                                        |
| Write latency        | Higher                                       | Lower                   | Depends on DB                              |
| Cache memory usage   | Higher                                       | Higher                  | Lower for new writes                       |
| Data-loss risk       | Lower                                        | Higher                  | Lower                                      |
| Cache consistency    | Stronger under successful coordinated writes | Eventual DB consistency | Requires invalidation for existing entries |

---

# 6. Consistency Comparison

A simplified consistency comparison:

```text
Write-Back → Write-Around → Write-Through
Least consistent              More consistent
```

### Write-Back

- Cache is updated immediately.
- Database is updated later.
- Cache and database can temporarily contain different values.
- Pending writes can be lost if not durably recorded.

### Write-Around

- Database is updated directly.
- New data is not added to the cache during writes.
- Existing cache entries must be invalidated to prevent stale reads.

### Write-Through

- Cache and database are updated synchronously.
- Both normally contain the latest value after a successful write.
- Failure handling is still required because two independent systems cannot be made atomic simply by writing synchronously.

**Note:** This is a general comparison, not an absolute consistency guarantee.

---

# 7. Write Availability Comparison

A simplified write-availability comparison:

```text
Write-Through → Write-Around → Write-Back
Lower availability             Higher availability
```

### Write-Through

Both cache and database must successfully participate in write.

### Write-Around

The database must be available. The cache is not required for the write operation.

### Write-Back

The cache must accept write, and the application can respond without waiting for the database.

However, the database must eventually become available for pending writes to be persisted.

**Note:** Actual availability depends on failure-handling policies, durable queues, replication, retry mechanisms, and the definition of a successful write.

---

# 8. When Should We Use Each Strategy?

| Scenario                                           | Suitable Strategy                                                |
|----------------------------------------------------|------------------------------------------------------------------|
| Product prices where reads should see updated data | Write-Through                                                    |
| High-volume counters and analytics                 | Write-Back                                                       |
| Data frequently written but rarely read            | Write-Around                                                     |
| Frequently accessed reference data                 | Write-Through                                                    |
| Large volumes of event-like data                   | Write-Back or Write-Around, depending on durability requirements |

---
