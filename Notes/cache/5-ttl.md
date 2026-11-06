# TTL (Time To Live) in System Design

## 1. What is TTL?

TTL stands for **Time To Live**.

It defines how long a piece of data should remain valid or available in a cache.

Once the TTL expires, the cache entry becomes expired and can no longer be treated as a valid cached value.

**In simple words:** TTL defines the lifetime of cached data.

### Why do we need TTL?

Cache stores frequently accessed data to improve application performance.

However, cached data can become outdated when the original data in the database changes.

TTL helps limit how long cached data remains available.

### Example

Initially, the database contains:

```text
Product ID: 101
Product Price: ₹80,000
```

Application fetches the product and stores it in Redis.

```text
Cache:
product:101 = ₹80,000
TTL = 5 minutes
```

Now the database price changes:

```text
Database:
product:101 = ₹90,000
```

But Redis still contains:

```text
Cache:
product:101 = ₹80,000
```

The cache contains stale data.

After 5 minutes, the cached entry expires.

On the next request, the application fetches the latest product price from the database and populates the cache again.

---

## 2. How TTL Works

Suppose we store a user session in Redis with a TTL of 60 seconds.

```text
Key: user:101
Value: User Session Data
TTL: 60 seconds
```

When the key is created, its expiration countdown begins.

```text
0 seconds  -> Key created
10 seconds -> Remaining TTL: 50 seconds
30 seconds -> Remaining TTL: 30 seconds
50 seconds -> Remaining TTL: 10 seconds
60 seconds -> Key expires
```

After expiration, the key is no longer available as a valid cache entry.

**Important:** Expiration does not necessarily mean that the key is physically deleted at the exact expiration instant. Redis can remove expired keys lazily when accessed and through background expiration work.

---

## 3. TTL in Redis

Redis supports setting expiration time on individual keys.

### Using SET and EXPIRE

```bash
SET user:101 "Piyush"
EXPIRE user:101 60
```

- SET stores the key and value.
- EXPIRE sets the TTL to 60 seconds.

### Setting value and expiration together

```bash
SET user:101 "Piyush" EX 60
```

Here, the key is created with a TTL of 60 seconds.

### Useful Redis TTL Commands

```bash
# Get remaining TTL in seconds
TTL user:101

# Get remaining TTL in milliseconds
PTTL user:101

# Remove expiration from a key
PERSIST user:101

# Set expiration in seconds
EXPIRE user:101 300

# Set expiration at a specific Unix timestamp
EXPIREAT user:101 1790000000
```

### TTL Command Return Values

| Return Value    | Meaning                          |
|-----------------|----------------------------------|
| Positive number | Remaining lifetime in seconds    |
| -1              | Key exists but has no expiration |
| -2              | Key does not exist               |

---

## 4. TTL vs Cache Eviction

Both TTL and Cache Eviction can remove data from the cache, but their purposes are different.

| Feature  | TTL                             | Cache Eviction                   |
|----------|---------------------------------|----------------------------------|
| Purpose  | Control data lifetime           | Free cache memory                |
| Trigger  | Expiration time reached         | Memory limit or eviction policy  |
| Based on | Time                            | LRU, LFU, FIFO, etc.             |
| Example  | Remove session after 30 minutes | Remove least recently used entry |

### Example

Suppose Redis has a key:

```text
user:101
TTL = 300 seconds
```

- If 300 seconds pass, the key expires because of TTL.
- If Redis reaches its configured memory limit before 300 seconds, an eviction policy may remove the key earlier.

**Remember:** TTL controls how long data remains valid, whereas eviction policies decide which entries to remove when memory needs to be freed.

---

## 5. Types of TTL

### A. Fixed TTL

The expiration time is set when the cache entry is created and does not automatically reset when the entry is accessed.

Example:

```text
User session TTL = 30 minutes

10:00 AM -> Session created
10:10 AM -> User accesses application
10:30 AM -> Session expires
```

Even though the user accessed the application at 10:10 AM, the original expiration time remains 10:30 AM.

### B. Sliding TTL (Refresh TTL)

The expiration time is refreshed whenever the cache entry is accessed.

Example:

```text
User session TTL = 30 minutes

10:00 AM -> Session created (expires at 10:30)
10:10 AM -> User accesses application
             TTL refreshed (expires at 10:40)
10:30 AM -> User accesses application
             TTL refreshed (expires at 11:00)
```

This is useful for session management, where active users should keep their sessions alive.

**Important:** Sliding TTL requires the application or cache layer to refresh the expiration on access. It is not the default behavior of every cache.

---

## 6. Does TTL Guarantee Cache and Database Consistency?

No.

TTL only limits how long a cached entry remains valid. It does not guarantee immediate consistency between the cache and database.

Example:

1. Database price is ₹80,000.
2. Redis stores ₹80,000 with a TTL of 10 minutes.
3. Database price changes to ₹90,000.
4. Until the cache expires or is explicitly invalidated, the application might return ₹80,000.

For better consistency, applications commonly combine:

- TTL-based expiration.
- Explicit cache invalidation after database updates.
- Event-driven invalidation when data changes.

**Remember:** TTL reduces the duration of stale data, but it does not eliminate stale data immediately.

---

## 7. Where is TTL Useful?

| Use Case                           | Example TTL                       |
|------------------------------------|-----------------------------------|
| User sessions                      | 30 minutes                        |
| OTP                                | 5 minutes                         |
| Product details                    | 5–15 minutes                      |
| Frequently changing dashboard data | 30–60 seconds                     |
| API response caching               | Depends on freshness requirements |
| Temporary verification tokens      | A few minutes                     |

These are illustrative values. Actual TTL depends on business requirements and how frequently the underlying data changes.

---

## 8. Interview Questions

**Q1. What is TTL?**

TTL (Time To Live) defines how long a cached entry should remain valid before it expires.

**Q2. Why do we need TTL?**

TTL helps prevent cached data from remaining available indefinitely and limits the duration of stale data.

**Q3. What happens when TTL expires?**

The cache entry becomes expired and cannot be returned as a valid cache hit. The next read can fetch fresh data from the database and populate the cache again.

**Q4. What is the difference between TTL and Cache Eviction?**

- TTL removes entries based on expiration time.
- Cache Eviction removes entries based on memory constraints and an eviction policy.

**Q5. What is Sliding TTL?**

Sliding TTL refreshes the expiration time whenever the cache entry is accessed.

**Q6. Does TTL guarantee cache consistency?**

No. TTL only limits the lifetime of cached data. Explicit or event-driven invalidation may be needed for faster consistency.

---

## 9. Quick Revision

- TTL = Time To Live.
- TTL defines the lifetime of cached data.
- Expired cache entries are no longer valid cache hits.
- Fixed TTL = Expiration time does not automatically refresh on access.
- Sliding TTL = Expiration time refreshes on access.
- TTL helps reduce stale data.
- TTL and Cache Eviction are different mechanisms.
- TTL does not guarantee immediate consistency between cache and database.