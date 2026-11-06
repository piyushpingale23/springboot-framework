# Cache Avalanche

## 1. What is Cache Avalanche?

Cache Avalanche occurs when a large number of cache entries expire or become unavailable at approximately the same time.

As a result, incoming requests cannot find data in the cache and start querying the database directly.

This can suddenly increase database load and potentially cause database overload or failure.

In simple words:

**Many cache entries expire together → Many cache misses → Huge number of database requests.**

## 2. Understand with an Example

Suppose an e-commerce application has the following products cached in Redis:

| Cache Key   | Product  | TTL       |
|-------------|----------|-----------|
| product:101 | Laptop   | 5 minutes |
| product:102 | Mobile   | 5 minutes |
| product:103 | Keyboard | 5 minutes |
| product:104 | Monitor  | 5 minutes |
| product:105 | Mouse    | 5 minutes |

Assume all these cache entries were created at approximately the same time with the same TTL of 5 minutes.

After 5 minutes, all these entries expire around the same time.

Now thousands of users request these products.

### Step-by-step flow

**Step 1: Before expiration**

Redis contains all product entries.

```text
product:101 -> Laptop
product:102 -> Mobile
product:103 -> Keyboard
product:104 -> Monitor
product:105 -> Mouse
```

Requests are served from Redis.

**Step 2: Cache entries expire together**

After 5 minutes:

```text
product:101 -> EXPIRED
product:102 -> EXPIRED
product:103 -> EXPIRED
product:104 -> EXPIRED
product:105 -> EXPIRED
```

**Step 3: Users send requests**

Many users request these products simultaneously.

- Application checks Redis.
- Redis returns Cache Miss.
- Application queries the database.
- A huge number of requests reach the database.

**Step 4: Database gets overloaded**

The database suddenly receives a large volume of queries.

This can cause:

- High database CPU utilization.
- Increased database connections.
- Increased response time.
- Request timeouts.
- Database overload or failure.

**The main problem:** A large number of cache entries become unavailable together, causing a sudden spike in database traffic.

## 3. How to Prevent Cache Avalanche?

### 1. TTL Jitter (Randomized TTL)

Instead of assigning the same TTL to every cache entry, add a random duration to each entry's TTL.

For example, instead of assigning 5 minutes to every product:

| Cache Key   | TTL          |
|-------------|--------------|
| product:101 | 5 min 10 sec |
| product:102 | 6 min 20 sec |
| product:103 | 4 min 45 sec |
| product:104 | 7 min 15 sec |
| product:105 | 5 min 50 sec |

Now, cache entries expire at different times.

This prevents many cache entries from expiring simultaneously.

**Example:**

```java
int baseTtl = 300; // 5 minutes

int randomTtl = ThreadLocalRandom.current()
        .nextInt(0, 121); // 0 to 120 seconds

int finalTtl = baseTtl + randomTtl;
```

Use `finalTtl` when storing the cache entry.

**Advantages:**

- Simple to implement.
- Spreads cache expiration over time.
- Reduces sudden database traffic spikes caused by synchronized expiration.

**Limitation:**

- Does not protect against Redis itself becoming unavailable.

### 2. Cache High Availability (Redis Replication / Cluster)

Another reason for Cache Avalanche is that the entire cache infrastructure becomes unavailable.

For example:

- Redis server crashes.
- Redis becomes unreachable.
- Network connectivity to Redis fails.

Even if cached entries have not expired, applications cannot access them.

To reduce this risk, use Redis High Availability.

Common approaches include:

- Redis Replication: Maintains replicas of Redis data.
- Redis Sentinel: Monitors Redis and supports automatic fail-over.
- Redis Cluster: Distributes data across multiple Redis nodes and supports sharding and high availability when configured with replicas.

**Advantages:**

- Reduces the risk of the entire cache becoming unavailable.
- Improves availability and fault tolerance.

**Limitation:**

- Adds infrastructure and operational complexity.
- Does not eliminate all failure scenarios.

### 3. Fallback / Graceful Degradation

If Redis becomes unavailable, the application can use a fallback strategy instead of sending unlimited requests to the database.

For example:

1. Application tries to fetch data from Redis.
2. Redis is unavailable.
3. Application applies a fallback strategy.
4. It may serve previously retained local cached data, if safe.
5. Otherwise, it may query the database with strict concurrency limits or return a controlled error.

Other useful protections include:

- Rate Limiting.
- Circuit Breaker.
- Bulkhead.
- Database connection limits.

**Advantages:**

- Prevents uncontrolled traffic from reaching the database.
- Helps the application remain responsive during cache failures.

**Limitation:**

- Some requests may receive stale data or temporary errors.
- Requires careful fallback design.

### 4. Cache Prewarming

Instead of waiting for users to request data after cache expiration, populate important cache entries before they are needed.

For example:

Suppose popular product cache entries expire at midnight.

A background job can refresh those entries before midnight.

```text
11:55 PM -> Refresh popular products in Redis
12:00 AM -> Existing TTLs expire
12:00 AM -> Refreshed entries are already available
```

**Advantages:**

- Reduces cache misses for frequently accessed data.
- Prevents sudden database traffic spikes for popular entries.

**Limitation:**

- Requires background jobs and refresh logic.
- Prewarming every possible cache entry may waste resources.

## 4. Comparison of Cache Avalanche Prevention Techniques

| Technique                       | How it helps                                              |
|---------------------------------|-----------------------------------------------------------|
| TTL Jitter                      | Prevents multiple cache entries from expiring together    |
| Redis High Availability         | Reduces the risk of complete cache infrastructure failure |
| Fallback / Graceful Degradation | Protects the database when cache becomes unavailable      |
| Cache Prewarming                | Loads important data before it is requested               |

## 5. Cache Stampede vs Cache Penetration vs Cache Avalanche

| Cache Stampede                                       | Cache Penetration                          | Cache Avalanche                                          |
|------------------------------------------------------|--------------------------------------------|----------------------------------------------------------|
| Many requests for the same expired cache entry       | Requests for non-existent data             | Many cache entries expire or become unavailable together |
| Data usually exists in DB                            | Data does not exist in DB                  | Data usually exists in DB                                |
| Many requests hit DB simultaneously for the same key | Repeated requests hit DB for missing data  | Many requests hit DB for different keys                  |
| Locking, TTL jitter, background refresh              | Negative caching, Bloom Filter, validation | TTL jitter, HA, fallback, prewarming                     |

### Remember

- Cache Stampede: One popular cache key expires, and many requests try to load it simultaneously.
- Cache Penetration: Requests repeatedly ask for data that does not exist.
- Cache Avalanche: Many cache entries expire together or the cache infrastructure becomes unavailable.
