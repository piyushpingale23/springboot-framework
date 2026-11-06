# Redis Internals and Distributed Caching Architecture

# Part 1: Redis Internals

## 1. What is Redis?

Redis (Remote Dictionary Server) is an open-source, in-memory data structure store commonly used as a distributed cache, database and message broker.

Unlike a traditional relational database, Redis primarily stores its working dataset in RAM, allowing very fast read and write operations.

### Common use cases

- Caching frequently accessed database records.
- Session management.
- Distributed locking.
- Rate limiting.
- Counters and leaderboards.
- Pub/Sub messaging.
- Temporary data storage.

## 2. How does Redis store data internally?

Redis stores data as key-value pairs.

Example:

```text
Key                 Value
--------------------------------
user:101            Piyush
product:101         Laptop
product:102         Mobile
session:abc123      UserSession
```

Redis supports several data types:

| Redis Data Type | Example                 | Common Use Case          |
|-----------------|-------------------------|--------------------------|
| String          | name = Piyush           | Simple key-value caching |
| Hash            | user:101 with name, age | Object-like data         |
| List            | [A, B, C]               | Queues                   |
| Set             | {101, 102, 103}         | Unique values            |
| Sorted Set      | Score-based members     | Leaderboards             |
| Stream          | Ordered event records   | Event processing         |

Example Redis commands:

```redis
SET user:101 "Piyush"
GET user:101
```

Using a Hash:

```redis
HSET user:101 name "Piyush" age 28
HGETALL user:101
```

Using a Sorted Set:

```redis
ZADD leaderboard 100 "user:101"
ZADD leaderboard 200 "user:102"
ZRANGE leaderboard 0 -1 WITHSCORES
```

## 3. Why is Redis fast?

The primary reason is that Redis keeps its working dataset in RAM rather than relying on disk for every read.

Additional reasons:

1. In-memory data access.
2. Efficient data structures.
3. Lightweight command processing.
4. Reduced network round trips using pipelining.
5. Atomic execution of individual Redis commands.

Performance also depends on network latency, serialization, data size, contention and workload.

## 4. Is Redis Single-Threaded?

Redis traditionally uses a primarily single-threaded command execution model.

Commands such as GET, SET, HSET and DEL are executed sequentially by the main command-processing thread.

### Why use this approach?

- Avoids much of the complexity of concurrent command execution.
- Reduces the need for locks around shared data structures.
- Makes individual command execution predictable and efficient.

However, modern Redis is not entirely single-threaded.

Redis 6 introduced optional I/O threading to handle some network read/write work in parallel.

**Remember:** Redis can handle many concurrent client connections, but ordinary commands are primarily executed sequentially by the main command-processing thread.

## 5. Does Redis store data permanently?

Redis is primarily an in-memory data store, but it supports persistence to protect data across restarts.

It provides two major persistence mechanisms.

### 1. RDB (Redis Database Backup)

RDB creates point-in-time snapshots of Redis data.

Example:

```text
10:00 AM -> Redis data snapshot
10:05 AM -> New data inserted
10:10 AM -> Redis data snapshot
```

Redis saves a snapshot of the dataset to an RDB file.

**Advantages:**

- Compact backup files.
- Useful for backups.
- Faster recovery in many scenarios.

**Limitation:**

- Changes made after the latest snapshot may be lost if Redis crashes.

### 2. AOF (Append Only File)

AOF records write operations so Redis can reconstruct the dataset by replaying them during recovery.

Example:

```redis
SET user:101 "Piyush"
SET product:101 "Laptop"
DEL user:101
```

These write operations are recorded in the AOF log.

**Advantages:**

- Can provide better durability than periodic snapshots, depending on the fsync policy.
- Helps reconstruct the latest dataset.

**Limitation:**

- AOF files can be larger.
- Recovery can take longer because commands need to be replayed.

### RDB vs AOF

| RDB                                     | AOF                                       |
|-----------------------------------------|-------------------------------------------|
| Stores snapshots                        | Records write operations                  |
| Compact backup                          | Usually larger than RDB                   |
| Changes after last snapshot can be lost | Durability depends on fsync configuration |
| Useful for backups                      | Useful for recovering recent changes      |

Redis can use both RDB and AOF together.

## 6. How does Redis manage limited RAM?

Redis stores its working dataset in memory, but RAM is limited.

Suppose Redis has a maximum memory configuration of 1 GB.

As data grows, it can reach that limit.

Redis supports eviction policies to manage memory.

| Policy       | Behavior                                        |
|--------------|-------------------------------------------------|
| noeviction   | Rejects writes when the memory limit is reached |
| allkeys-lru  | Evicts least recently used keys                 |
| allkeys-lfu  | Evicts least frequently used keys               |
| volatile-lru | Evicts least recently used keys with TTL        |
| volatile-ttl | Evicts keys with the shortest remaining TTL     |

Redis eviction policies are configurable. Redis does not automatically use LRU in every setup.

---

# Part 2: Distributed Caching Architecture

## 7. What is Distributed Caching?

Distributed caching means maintaining cached data in a shared caching system that can be accessed by multiple application instances.

Consider an application deployed on three servers.

### Without Distributed Caching

Each application maintains its own local cache.

```text
App 1 -> Local Cache -> Product 101
App 2 -> Local Cache -> Product 101
App 3 -> Local Cache -> Product 101
```

Problems:

- Each instance maintains a separate copy of the data.
- Cache entries can become inconsistent.
- An update in App 1 does not automatically update App 2 or App 3.
- The same data consumes memory in multiple application instances.

### With Distributed Caching

All application instances access the same Redis cache.

```text
App 1 ----\
App 2 -----+----> Shared Redis Cache
App 3 ----/
```

**Advantages:**

- Shared cache across application instances.
- Less duplication of cached data.
- Centralized cache management.
- Easier coordination of cache invalidation.

## 8. How does a request work in Distributed Caching?

Suppose we have three Spring Boot application instances and one shared Redis server.

A user requests product ID 101.

```text
Client
  |
  v
Load Balancer
  |
  v
Spring Boot Application (Instance 1)
  |
  v
Redis Cache
  |
  |-- Cache HIT --> Return product
  |
  |-- Cache MISS
         |
         v
      Database
         |
         v
      Store in Redis
         |
         v
      Return product
```

Now suppose the next request reaches Instance 2.

Instance 2 checks the same Redis server and finds the cached product.

It does not need to query the database again.

This is one of the most common distributed caching architectures.

## 9. Redis Replication

Replication means maintaining copies of data on one or more Redis servers.

Redis uses:

- Primary (Master): Accepts writes and handles reads by default.
- Replica (Slave): Receives data from the primary and can serve reads.

### Architecture

```text
             Primary Redis
              Read + Write
                   |
          Asynchronous Replication
             /           \
            v             v
       Replica 1      Replica 2
       Optional       Optional
       Read           Read
```

### How does it work?

1. Application writes data to the primary Redis server.
2. Primary Redis propagates changes to its replicas.
3. Replicas maintain copies of the primary's data.
4. Applications can optionally distribute read traffic across replicas.

**Important points:**

- Redis replication is asynchronous by default.
- A replica can temporarily have stale data because replication is not instantaneous.
- If the primary crashes, a replica can potentially be promoted.
- Replication alone does not automatically provide failover.

## 10. Redis Sentinel

Redis Sentinel provides monitoring and automatic failover for Redis deployments using primary-replica replication.

Suppose the primary Redis server crashes.

Without automatic failover, someone may need to manually promote a replica.

Redis Sentinel helps automate this process.

### Architecture

```text
       Primary Redis
       Server Failure
              |
              v
       Redis Sentinel
       Monitoring and
       Failover Coordination
              |
              v
       Replica Promoted
              |
              v
        New Primary
```

### How does it work?

1. Sentinels continuously monitor Redis servers.
2. They detect when the primary becomes unavailable.
3. Sentinels use a quorum-based process to agree that the primary is objectively down.
4. A suitable replica is selected and promoted to primary.
5. Other replicas are reconfigured to replicate from the new primary.
6. Sentinel-aware clients discover the new primary.

**Important:**

- Redis Sentinel provides high availability.
- It does not shard data.
- It manages failover for a replicated Redis deployment.

## 11. Redis Cluster (Sharding)

What happens when the Redis dataset becomes too large for one Redis server?

Instead of storing all data on one server, we can distribute data across multiple Redis nodes.

This is called **Sharding**.

Redis Cluster uses 16,384 hash slots to distribute keys across the cluster.

A key is assigned to a hash slot using:

```text
HASH_SLOT = CRC16(key) mod 16384
```

The cluster assigns ranges of hash slots to different primary nodes.

### Example Architecture

```text
                 Application
                      |
                      v
                Redis Cluster
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
       Primary 1   Primary 2   Primary 3
       Slots       Slots       Slots
       0-5460      5461-10922  10923-16383
          |           |           |
          v           v           v
       Replica 1   Replica 2   Replica 3
```

### How does it work?

Suppose an application stores:

```text
product:101
product:102
user:101
user:102
```

1. Redis Cluster calculates the hash slot for each key.
2. Based on the slot, the key is assigned to a primary node.
3. That primary stores the key.
4. Its replica receives the replicated data.

If a primary fails, Redis Cluster can promote an eligible replica through its failover mechanism.

**Advantages:**

- Distributes data across multiple Redis servers.
- Provides horizontal scalability.
- Supports replication and failover.
- Avoids relying on a single Redis server for the entire dataset.

**Limitations:**

- More operational complexity.
- Multi-key operations have restrictions when keys belong to different hash slots.
- Replication is asynchronous, so acknowledged writes can potentially be lost during failover.

## 12. Redis Sentinel vs Redis Cluster

| Redis Sentinel                         | Redis Cluster                            |
|----------------------------------------|------------------------------------------|
| Provides high availability             | Provides sharding and high availability  |
| Does not shard data                    | Distributes data across nodes            |
| Primary-replica architecture           | Multiple primary-replica groups          |
| Suitable when data fits on one primary | Suitable for larger distributed datasets |

## 13. Cache Consistency in Distributed Architecture

Consider three application instances sharing Redis and a database.

Initially:

```text
Database: product:101 = Laptop
Redis:    product:101 = Laptop
```

Now App 1 updates the product:

```text
Database: product:101 = Gaming Laptop
```

But suppose the Redis entry is not invalidated.

```text
Database: product:101 = Gaming Laptop
Redis:    product:101 = Laptop
```

Now App 2 may return the old value.

This is a cache consistency problem.

### Common Solution: Cache Invalidation

After a successful database update:

```java
@Transactional
public void updateProduct(Long id, String name) {

    Product product = productRepository.findById(id)
            .orElseThrow();

    product.setName(name);
    productRepository.save(product);

    redisTemplate.delete("product:" + id);
}
```

The next request will:

1. Check Redis.
2. Find a cache miss.
3. Fetch the latest data from the database.
4. Populate Redis again.

**Important:** Database transactions do not automatically make Redis operations atomic with the database transaction.

For reliable distributed systems, consider after-commit invalidation, retries and the Transactional Outbox pattern.

## 14. Complete Distributed Caching Architecture

```text
                  Clients
                     |
                     v
                Load Balancer
                     |
          +----------+----------+
          |          |          |
          v          v          v
        App 1      App 2      App 3
          |          |          |
          +----------+----------+
                     |
                     v
                Redis Cluster
              Sharding + Replication
                     |
                     v
                  Database
                Source of Truth
```

In this architecture:

- Load Balancer distributes requests across application instances.
- Application instances use the shared Redis cluster.
- Redis provides fast access to cached data.
- Redis Cluster distributes cache data across nodes.
- Replicas improve availability.
- Database remains the source of truth.

---

# Part 3: Interview Preparation

## 15. Redis Components — Quick Revision

| Redis Component | Primary Responsibility                     |
|-----------------|--------------------------------------------|
| Redis Server    | Stores and processes data in memory        |
| RDB             | Creates snapshots                          |
| AOF             | Records write operations for recovery      |
| Eviction Policy | Manages memory when maxmemory is reached   |
| Replication     | Maintains copies of data                   |
| Sentinel        | Monitors servers and manages failover      |
| Redis Cluster   | Shards data and supports high availability |

## 16. Important Interview Questions

1. What is Redis and why is it faster than a traditional database for many cache workloads?
2. Why is Redis primarily single-threaded for command execution?
3. How does Redis store data internally?
4. What is the difference between RDB and AOF?
5. How does Redis manage limited memory?
6. What is Redis replication?
7. What happens when the Redis primary server crashes?
8. What is Redis Sentinel?
9. What is Redis Cluster?
10. What is the difference between Redis Sentinel and Redis Cluster?
11. How does Redis Cluster distribute keys?
12. What are the 16,384 hash slots?
13. How can multiple Spring Boot instances share the same Redis cache?
14. How do you handle cache consistency in a distributed architecture?
