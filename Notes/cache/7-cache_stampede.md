# Cache Stampede in System Design

## 1. What is Cache Stampede?

Cache Stampede is a caching problem that occurs when a popular cache entry expires or becomes unavailable, causing many concurrent requests to fetch the same data from the database at the same time.

It is also commonly called the **Thundering Herd Problem**.

**In simple words:** When a frequently accessed cache entry expires, multiple application requests may simultaneously query the database to fetch the same data.

---

## 2. Why Does Cache Stampede Happen?

Suppose an e-commerce application has a popular product:

```text
Product ID: 101
Product Name: iPhone
Product Price: ₹80,000
```

This product is requested by 10,000 users per minute.

To improve performance, the application stores its details in Redis.

```text
Redis:
product:101 = Product Details
TTL = 5 minutes
```

As long as the cache entry exists, application requests can retrieve product details directly from Redis.

But what happens when the cache entry expires?

The cache entry becomes unavailable.

Now multiple requests arrive simultaneously:

1. Request 1 checks Redis -> Cache Miss.
2. Request 2 checks Redis -> Cache Miss.
3. Request 3 checks Redis -> Cache Miss.
4. Request 4 checks Redis -> Cache Miss.
5. Hundreds or thousands of requests also experience Cache Miss.

All these requests independently query the database for the same product.

### Cache Stampede Flow

```text
          1,000 Concurrent Requests
                     |
                     v
                 Redis Cache
                     |
                 Cache Miss
                     |
                     v
                  Database
                     |
          +----------+----------+
          |          |          |
          v          v          v
       Query 1    Query 2    Query 3
          |          |          |
          +----------+----------+
                     |
                     v
             Same Product Data
```

### Possible Consequences

- High database CPU utilization.
- Increased database connections.
- Increased query execution time.
- Request timeouts.
- Database overload or failure.

---

## 3. How to Prevent Cache Stampede?

There are four common techniques:

1. Locking / Distributed Lock
2. TTL Jitter
3. Background Refresh / Refresh Ahead
4. Stale-While-Revalidate

---

## 4. Locking / Distributed Lock

The idea is to allow only one request to fetch missing data from the database.

Other requests should wait or retry checking the cache until the data becomes available.

### Workflow

1. Request 1 checks Redis -> Cache Miss.
2. Request 1 acquires a lock for `product:101`.
3. Request 1 fetches product data from the database.
4. Other requests cannot acquire the lock.
5. Other requests wait briefly or retry checking Redis.
6. Request 1 stores the data in Redis and releases the lock.
7. Other requests retrieve the data from Redis.

### Flow

```text
          5 Concurrent Requests
                    |
                    v
                Redis Cache
                    |
                Cache Miss
                    |
                    v
             Distributed Lock
                    |
             +------+------+
             |             |
          Request 1    Other Requests
             |             |
        Acquires Lock   Wait / Retry
             |
             v
          Database
             |
             v
        Fetch Data
             |
             v
        Update Cache
             |
             v
        Release Lock
             |
             v
      Other Requests Read Cache
```

### Redis Lock Example

```bash
SET lock:product:101 unique-token NX EX 10
```

- `NX`: Create the key only if it does not already exist.
- `EX 10`: Set a 10-second expiration on the lock.
- `unique-token`: Unique owner token used to verify lock ownership.

### Important Points

- Only one request should load the missing data.
- Other requests can wait or retry.
- Lock should have an expiration time to avoid permanent locks.
- Lock should be released only by its owner.
- Use an atomic compare-and-delete operation when releasing the lock.
- Handle lock expiration and loader failures properly.

---

## 5. TTL Jitter (Randomized TTL)

TTL Jitter means adding a random amount of time to the configured TTL.

It prevents many cache entries from expiring at exactly the same time.

### Example

Suppose 10,000 product entries are cached with the same TTL of 5 minutes.

They may all expire around the same time, causing a sudden increase in database requests.

Instead, add a random amount of time to the TTL.

```text
Product A -> TTL = 5 minutes + 20 seconds
Product B -> TTL = 5 minutes + 45 seconds
Product C -> TTL = 5 minutes + 10 seconds
Product D -> TTL = 5 minutes + 35 seconds
```

Now their expiration times are spread across different moments.

### Advantages

- Reduces simultaneous expiration of many cache entries.
- Helps distribute database load over time.
- Simple to implement.

### Limitation

TTL Jitter does not fully solve a stampede on one extremely popular key that expires at a particular moment.

---

## 6. Background Refresh / Refresh Ahead

In this approach, the application refreshes cache data before the existing entry expires.

### Example

```text
Cache TTL = 5 minutes

10:00 AM -> Cache populated
10:04 AM -> Background refresh starts
10:04 AM -> Fetch latest data from database
10:04 AM -> Update cache
10:05 AM -> Original expiration time
```

The idea is to refresh the cache before it expires so incoming requests can continue reading cached data.

### Advantages

- Reduces cache misses for popular entries.
- Avoids many requests querying the database simultaneously.
- Useful for predictable, frequently accessed data.

### Disadvantages

- Requires background refresh logic.
- Can generate unnecessary database queries for data that nobody requests.
- Refresh failures need to be handled.

---

## 7. Stale-While-Revalidate (SWR)

In this approach, the application is allowed to serve slightly outdated data while a background process refreshes the cache.

### Example

```text
Product Price = ₹80,000

Fresh TTL = 5 minutes
Stale allowance = 2 minutes
```

After the fresh TTL expires:

1. Cache entry is considered stale but is retained.
2. Application serves the stale value if business rules allow it.
3. Only one background request refreshes the cache.
4. New data is stored in Redis.
5. Subsequent requests receive fresh data.

### Flow

```text
          Client Request
                |
                v
          Check Cache
                |
                v
          Cache is Stale
                |
          +-----+------+
          |            |
          v            v
     Return Stale   Background
        Data         Refresh
                       |
                       v
                    Database
                       |
                       v
                   Update Cache
                       |
                       v
                   Fresh Data
```

### Advantages

- Maintains low response latency.
- Reduces database load.
- Avoids blocking all requests while data is refreshed.

### Disadvantages

- Users may temporarily receive stale data.
- Requires background refresh logic.
- Not suitable for data where stale values can cause serious business problems.

**Important:** Do not use stale data for operations where correctness is critical, such as financial balances or payment authorization.

---

## 8. Comparison of Prevention Techniques

| Technique              | How it prevents stampede                | Main Idea             |
|------------------------|-----------------------------------------|-----------------------|
| Locking                | Allows one request to load missing data | Single loader         |
| TTL Jitter             | Spreads expiration times across keys    | Randomized expiration |
| Background Refresh     | Refreshes data before expiration        | Refresh ahead         |
| Stale-While-Revalidate | Serves stale data while refreshing      | Don't block readers   |

### Which Technique Should We Use?

- **Locking:** When multiple requests may load the same missing cache entry.
- **TTL Jitter:** When many different cache entries might expire together.
- **Background Refresh:** When popular data can be refreshed before expiration.
- **Stale-While-Revalidate:** When slightly stale data is acceptable and low latency is important.

These techniques can also be combined.

For example, an application can use TTL Jitter across cache keys and distributed locking for individual popular keys.

---

## 9. Cache Stampede vs Cache Penetration vs Cache Avalanche

| Problem           | Meaning                                                                                          |
|-------------------|--------------------------------------------------------------------------------------------------|
| Cache Stampede    | Many requests fetch the same missing or expired cache entry simultaneously.                      |
| Cache Penetration | Requests repeatedly ask for data that does not exist in the database, bypassing the cache.       |
| Cache Avalanche   | Many cache entries expire or become unavailable together, causing a sudden load on the database. |

### Remember the Difference

- Stampede = Many requests for the same missing key.
- Penetration = Requests for non-existent data.
- Avalanche = Many cache entries fail or expire together.

---

## 10. Interview Questions

**Q1. What is Cache Stampede?**

Cache Stampede occurs when multiple concurrent requests try to fetch the same data from the database after a popular cache entry expires or becomes unavailable.

**Q2. Why does Cache Stampede happen?**

Because multiple requests observe a cache miss before any request has completed loading and populating the cache.

**Q3. How can we prevent Cache Stampede?**

Common techniques include:

- Distributed Locking
- TTL Jitter
- Background Refresh
- Stale-While-Revalidate

**Q4. What is the purpose of a distributed lock?**

It ensures that only one request performs the database loading operation for a particular missing cache key while other requests wait or retry.

**Q5. What is TTL Jitter?**

TTL Jitter adds a random amount of time to cache expiration so that many cache entries do not expire simultaneously.

**Q6. What is Stale-While-Revalidate?**

It allows the application to serve slightly stale data while a background process fetches and stores fresh data.

**Q7. What is the Thundering Herd Problem?**

It is another name commonly used for Cache Stampede, where many concurrent requests attempt the same expensive operation after a shared resource becomes unavailable.

---

## 11. Quick Revision

- Cache Stampede = Many concurrent requests query the database for the same missing cache entry.
- Distributed Lock = Allows one request to load the missing data.
- TTL Jitter = Randomizes expiration times.
- Background Refresh = Refreshes cache before expiration.
- Stale-While-Revalidate = Serves stale data while refreshing it in the background.
- Cache Stampede is also called the Thundering Herd Problem.