# Database Sharding — Complete Notes

## 1. What Is Sharding?

Sharding is a database scaling technique in which a large dataset is divided horizontally into smaller subsets called **shards**.

Each shard stores a subset of the records and can be hosted on a separate database server.

For example, instead of storing 1 crore customer records on one database server, we distribute the records across multiple database servers.

### Architecture

```text
                 Application
                      |
              Sharding / Routing
                      |
          +-----------+-----------+
          |           |           |
       Shard 0     Shard 1     Shard 2
       DB Server   DB Server   DB Server
       Customers   Customers   Customers
       1-1000      1001-2000   2001-3000
```

### Why Do We Need Sharding?

- Handle large volumes of data.
- Distribute database load across multiple servers.
- Increase write and storage capacity.
- Reduce the workload on individual database servers.
- Scale horizontally by adding database servers.

**Important:** Sharding distributes data. It does not automatically provide backup or high availability.

---

## 2. Sharding vs. Replication

These are two different database scaling techniques.

### Sharding

Sharding distributes different subsets of data across servers.

```text
Shard 0: Customer A, B, C
Shard 1: Customer D, E, F
Shard 2: Customer G, H, I
```

Each shard contains a different subset of records.

### Replication

Replication maintains copies of the same data on multiple servers.

```text
Primary Database
       |
       +------> Replica 1
       |
       +------> Replica 2
```

The replicas contain copies of the primary's data, subject to replication lag and configuration.

### Comparison

| Feature           | Sharding                         | Replication                         |
|-------------------|----------------------------------|-------------------------------------|
| Main purpose      | Distribute data and load         | Maintain copies of data             |
| Data stored       | Different subsets                | Copies of the same dataset          |
| Write scaling     | Can increase across shards       | Depends on replication architecture |
| Read scaling      | Distributes reads across shards  | Replicas can serve additional reads |
| High availability | Not guaranteed by sharding alone | Can support high availability       |

Sharding and replication can be used together. Each shard can have its own replicas.

---

## 3. Types of Sharding Strategies

The main strategies are:

1. Range-based sharding
2. Hash-based sharding

### 3.1 Range-Based Sharding

Data is distributed according to ranges of a selected key.

For example, shard customers by `customer_id`.

```text
Shard 0: customer_id 1 - 1000
Shard 1: customer_id 1001 - 2000
Shard 2: customer_id 2001 - 3000
```

A record with `customer_id = 1500` belongs to Shard 1.

#### Advantages

- Simple to understand and implement.
- Efficient for range queries.
- Easy to identify the destination shard when ranges are well-defined.

#### Disadvantages

- Uneven distribution can occur.
- One shard may receive more traffic than others.
- Sequential keys can create hotspots.

#### Example

If most new orders have increasing IDs, the latest range may receive nearly all write traffic.

### 3.2 Hash-Based Sharding

A hash function is applied to a sharding key to determine the destination shard.

A common modulus-based implementation is:

```
shardIndex = hash(key) % numberOfShards;
```

Suppose there are four shards:

```text
hash(key) % 4

Result 0 -> Shard 0
Result 1 -> Shard 1
Result 2 -> Shard 2
Result 3 -> Shard 3
```

For example:

```text
hash(customer_id = 101) = 17

17 % 4 = 1

Destination: Shard 1
```

#### Advantages

- Can distribute data evenly when keys are well distributed.
- Avoids the sequential-key hotspot common with some range-based designs.
- Routing can be fast.

#### Disadvantages

- Range queries may need to access multiple shards.
- Changing the shard count can remap many records when using ordinary modulus routing.
- Hash collisions and skewed access patterns still need consideration.

**Important:** Hash-based sharding is a broad category. Modulus-based sharding and consistent hashing are different ways to use hashing for shard placement.

---

## 4. What Is a Sharding Key?

A sharding key is the field used to determine the shard that stores a record.

Examples:

```text
customer_id
order_id
tenant_id
region_id
```

The choice of key affects data distribution, query performance, and scalability.

### Example: Customer-Based Sharding

```text
customer_id = 101 -> Shard 1
customer_id = 102 -> Shard 2
customer_id = 103 -> Shard 0
```

The actual destination depends on the chosen sharding strategy.

### How to Choose a Sharding Key

Consider:

- **Cardinality:** Does the key have enough distinct values?
- **Distribution:** Will records be distributed reasonably evenly?
- **Access patterns:** Which key is commonly used in queries?
- **Hotspots:** Can one key or tenant generate disproportionate traffic?
- **Locality:** Should related records reside on the same shard?
- **Growth:** Will the key still work as data volume increases?

For example, `customer_id` may be a better choice than `country` if most customers belong to only a few countries and those countries have very different traffic volumes.

---

## 5. Deterministic vs. Non-Deterministic Hashing

### 5.1 Deterministic Hashing

A deterministic hash function produces the same output for the same input under the same conditions.

```text
hash("customer-101") -> 451
hash("customer-101") -> 451
hash("customer-101") -> 451
```

This property is important for sharding because the application must consistently locate an existing record.

### 5.2 Non-Deterministic Behavior

A non-deterministic function may produce different results for the same input across invocations.

```text
function("customer-101") -> 451
function("customer-101") -> 829
function("customer-101") -> 217
```

If such behavior determines a record's destination without any persistent mapping, the application may look in the wrong shard.

However, a deterministic hash does not automatically guarantee stable shard placement. Changing the number of shards in a modulus formula can still change the destination.

**Key distinction:**

- Deterministic hashing: same input produces the same hash under the same conditions.
- Stable shard placement: the same key continues to map to the same shard when the cluster changes.

Consistent hashing helps improve the second property when shard membership changes.

---

## 6. Modulus-Based Sharding

Modulus-based sharding calculates the destination using the remainder operator.

```
shardIndex = hash(key) % numberOfShards;
```

### Example: Three Shards

```text
numberOfShards = 3

hash(key) = 10

10 % 3 = 1

Destination: Shard 1
```

In Java, use `Math.floorMod()` when a hash may be negative:

```java
int shardIndex = Math.floorMod(
    key.hashCode(),
    numberOfShards
);
```

### What Happens When We Add a Shard?

Initially:

```
shardIndex = hash(key) % 3;
```

After adding a fourth shard:

```
shardIndex = hash(key) % 4;
```

Many records may receive different destinations.

| Hash value | With 3 shards | With 4 shards |
|-----------:|--------------:|--------------:|
|         10 |  `10 % 3 = 1` |  `10 % 4 = 2` |
|         12 |  `12 % 3 = 0` |  `12 % 4 = 0` |
|         15 |  `15 % 3 = 0` |  `15 % 4 = 3` |
|         20 |  `20 % 3 = 2` |  `20 % 4 = 0` |
|         24 |  `24 % 3 = 0` |  `24 % 4 = 0` |

Some records stay on the same shard number, while others move.

### Approximate Data Redistribution

Under a uniform hash distribution, when increasing from three to four shards:

- Approximately 75% of records change their shard assignment.
- Approximately 25% retain the same shard number.

When reducing from four shards to three, the same approximate proportions apply.

These are expected proportions for ordinary modulus routing, not guarantees for every dataset.

**Important:** Changing the shard count does not automatically move existing database records. A controlled migration process is required.

---

## 7. What Is Consistent Hashing?

Consistent hashing is a hashing technique that minimizes data redistribution when nodes or shards are added or removed.

Instead of calculating:

```
shardIndex = hash(key) % numberOfShards;
```

consistent hashing places shard identifiers and data keys on a logical circular hash space called a **hash ring**.

The destination is determined by the key's position on the ring and the positions of the available shards.

### Main Components

1. Hash function
2. Hash ring
3. Shard positions
4. Data-key positions
5. Clockwise lookup rule

### Step 1: Place Shards on the Ring

Suppose the ring has hash positions from `0` to `99`.

The shard identifiers are hashed to positions:

```text
Shard A -> Position 10
Shard B -> Position 40
Shard C -> Position 75
```

Conceptually:

```text
              Hash Ring

        10: Shard A
       /           \
      /             \
  75: Shard C      40: Shard B
```

The ring wraps around: after the largest position, lookup continues from the smallest position.

The positions above are illustrative.

#### What Do the Other Positions Contain?

The ring has 100 possible positions, but only three positions are occupied by shard identifiers in this example.

```text
Position 10 -> Shard A
Position 40 -> Shard B
Position 75 -> Shard C

All other positions -> Unused hash positions
```

These unused positions do not contain any special data. They are simply possible locations in the hash space.

**Important:** The hash ring is a logical structure maintained by the routing system. It is not a physical database table containing records at every position.

#### What Does the Hash Function Actually Calculate?

In our example, we assume a hash space from `0` to `99` to make the concept easy to understand.

A simplified Java example is:

```java
// Illustrative example: map a key into positions 0-99.
int position = Math.floorMod(key.hashCode(), 100);
```

For example:

```text
hash("Shard-A")    -> Position 10
hash("Shard-B")    -> Position 40
hash("Customer-1") -> Position 15
```

These positions are illustrative, not guaranteed outputs of Java's `hashCode()`.

In a real implementation, the hash function typically produces a much larger range of values, such as a 32-bit or 64-bit hash space.

For example, a 32-bit unsigned hash space has positions from `0` to `2^32 - 1`, providing over four billion possible positions.

```text
Shard A      -> Position 800,000,000
Shard B      -> Position 2,100,000,000
Shard C      -> Position 3,500,000,000

Customer-101 -> Position 1,200,000,000
```

The routing system uses the relative positions of the keys and shards to determine ownership.

**Key distinction:** Consistent hashing does not ordinarily calculate `hash(key) % numberOfShards`. It hashes the key into the ring's hash space and finds the next shard clockwise.

### Step 2: Place Data Keys on the Ring

Hash each data key to determine its position.

```text
Customer-101 -> Position 15
Customer-102 -> Position 50
Customer-103 -> Position 80
```

Data keys and shard identifiers are hashed independently into the same logical hash space.

For example:

```text
Shard A       -> Position 10
Customer-101  -> Position 15
Shard B       -> Position 40
Customer-102  -> Position 50
Shard C       -> Position 75
Customer-103  -> Position 80
```

The data-key positions do not need to be occupied by shards. They represent where the keys fall in the hash space.

#### How Do We Find the Destination Shard?

Follow these steps:

1. Hash the data key.
2. Find its position on the ring.
3. Move clockwise from that position.
4. Select the first shard encountered.
5. If no shard remains before the end of the ring, wrap around to the first shard.

Example:

| Data key     | Key position | First shard clockwise        | Destination |
|--------------|-------------:|------------------------------|-------------|
| Customer-101 |           15 | Shard B at 40                | Shard B     |
| Customer-102 |           50 | Shard C at 75                | Shard C     |
| Customer-103 |           80 | Shard A at 10 after wrapping | Shard A     |

The destination is determined by the ring, not directly by the total number of shards.

**Remember:** The hash function determines a position; the clockwise lookup rule determines the shard.

### Step 3: Add a New Shard

Suppose we add Shard D at position 60.

Before:

```text
Shard A -> 10
Shard B -> 40
Shard C -> 75
```

After:

```text
Shard A -> 10
Shard B -> 40
Shard D -> 60  // New shard
Shard C -> 75
```

Now examine the data:

| Data key     | Position | Before  | After   |
|--------------|---------:|---------|---------|
| Customer-101 |       15 | Shard B | Shard B |
| Customer-102 |       50 | Shard C | Shard D |
| Customer-103 |       80 | Shard A | Shard A |

Only Customer-102 changes its destination in this example.

Shard D takes ownership of keys that previously belonged to the next shard clockwise, Shard C, within the affected range.

Keys outside that range generally keep their original destinations.

### Step 4: Remove a Shard

Suppose Shard D is removed.

Before:

```text
Shard A -> 10
Shard B -> 40
Shard D -> 60
Shard C -> 75
```

After:

```text
Shard A -> 10
Shard B -> 40
Shard C -> 75
```

Keys previously assigned to Shard D now route to Shard C, the next shard clockwise.

Keys assigned to Shards A and B generally remain where they are.

**This is the key benefit of consistent hashing:** adding or removing a shard affects only a portion of the hash ring, rather than forcing a new modulus calculation for every record.

---

## 8. Modulus-Based Sharding vs. Consistent Hashing

| Feature            | Modulus-based sharding               | Consistent hashing                               |
|--------------------|--------------------------------------|--------------------------------------------------|
| Routing            | `hash(key) % N`                      | Next shard clockwise on the ring                 |
| Add a shard        | Can remap many records               | Usually moves a subset                           |
| Remove a shard     | Can remap many records               | Usually moves records owned by the removed shard |
| Implementation     | Simpler                              | More complex                                     |
| Membership changes | Potentially extensive redistribution | Limited redistribution                           |
| Load distribution  | Depends on key distribution          | Improved with virtual nodes                      |
| Data migration     | Required when destinations change    | Required for affected keys                       |

### How Much Data Moves?

Assume there are 12,000 records distributed evenly.

| Operation           |            Modulus-based sharding |                Consistent hashing |
|---------------------|----------------------------------:|----------------------------------:|
| Add shard: 3 → 4    | Approximately 9,000 records (75%) | Approximately 3,000 records (25%) |
| Remove shard: 4 → 3 | Approximately 9,000 records (75%) | Approximately 3,000 records (25%) |

These figures are illustrative expectations under uniform hashing and balanced shard placement.

Consistent hashing does not guarantee that exactly 25% of records move. The actual amount depends on ring placement, key distribution, and shard capacity.

Also, these figures describe records whose destination changes, not necessarily the exact amount of data transferred during an implementation's migration process.

---

## 9. What Are Virtual Nodes?

Virtual nodes, also called vnodes, are multiple logical positions on the hash ring that belong to the same physical shard.

For example:

```text
Physical Shard A -> A1, A2, A3, A4
Physical Shard B -> B1, B2, B3, B4
Physical Shard C -> C1, C2, C3, C4
```

Each virtual node has a separate position on the ring, but all virtual nodes belonging to Shard A store data on the same physical database.

### Why Are Virtual Nodes Useful?

Without virtual nodes, one physical shard may accidentally own a much larger section of the ring than another.

Virtual nodes help:

- Distribute ring ownership more evenly.
- Reduce the impact of an unlucky hash placement.
- Balance load more effectively.
- Allow capacity-aware placement when configured appropriately.

Virtual nodes improve distribution but do not guarantee perfectly equal data volume or traffic. Hot keys and uneven record sizes can still create hotspots.

---

## 10. Range-Based vs. Modulus vs. Consistent Hashing

| Feature                | Range-based              | Modulus-based                    | Consistent hashing                        |
|------------------------|--------------------------|----------------------------------|-------------------------------------------|
| Placement              | Key ranges               | Hash remainder                   | Hash ring                                 |
| Range queries          | Usually efficient        | Often require multiple shards    | Often require multiple shards             |
| Sequential-key hotspot | Possible                 | Less likely with a suitable hash | Less likely with a suitable hash          |
| Adding a shard         | Split or assign ranges   | May remap many records           | Usually moves a subset                    |
| Removing a shard       | Merge or reassign ranges | May remap many records           | Usually moves records owned by that shard |
| Complexity             | Low to medium            | Low                              | Medium to high                            |

---

## 11. Sharding in a Spring Boot Application

Spring Boot does not automatically implement database sharding merely because you use Spring Data JPA.

A routing layer must determine which database should handle a request.

### Architecture

```text
Client
  |
  v
API Gateway
  |
  v
Spring Boot Service
  |
  v
Shard Routing Layer
  |
  +-------> Shard 0 DataSource
  |
  +-------> Shard 1 DataSource
  |
  +-------> Shard 2 DataSource
```

### Conceptual Routing Example

```java
public class ShardRouter {

    public DataSource getShard(String key) {

        // 1. Hash the data key.
        // 2. Find the destination using the selected strategy.
        // 3. Return the corresponding DataSource.

        return findShard(key);
    }
}
```

The `findShard()` method represents a routing implementation; it is not a built-in Spring method.

A real implementation must also handle transactions, shard membership, connection pools, migration, and error recovery.

### What Happens When a Shard Is Added?

1. Provision the new database shard.
2. Update the routing metadata or hash ring.
3. Identify the records whose destinations change.
4. Migrate those records safely.
5. Coordinate reads and writes during migration.
6. Verify data consistency.
7. Complete the routing cutover.

Adding a shard to the configuration without migrating existing data can cause records to be missing from the location where the application expects them.

---

## 12. Important Challenges in Sharding

### Cross-Shard Queries

A query may need to access several shards.

For example:

```sql
SELECT *
FROM orders
WHERE order_date >= '2026-01-01';
```

If `order_date` is not the sharding key, the query may need to run on multiple shards and combine the results.

### Cross-Shard Transactions

A transaction that modifies records on different shards is more complex than a transaction contained within one database.

Distributed transaction coordination or an application-level consistency pattern may be needed.

### Hotspots

One shard may receive significantly more traffic than others because of a popular key, tenant, or uneven distribution.

### Re-balancing

Adding or removing shards requires routing changes and, when record destinations change, a safe data migration process.

### Global Uniqueness

Generating globally unique IDs across multiple independent databases requires an appropriate ID-generation strategy.

### Operational Complexity

Monitoring, backups, schema migrations, connection management, and incident recovery become more complicated.

---

## 13. Common Interview Questions

### Q1. What is sharding?

Sharding is horizontal database partitioning in which different subsets of records are distributed across multiple database servers.

### Q2. What is the difference between sharding and replication?

Sharding distributes different subsets of data. Replication maintains copies of data on multiple servers.

### Q3. What is a sharding key?

It is the field used to determine where a record is stored.

### Q4. What is modulus-based sharding?

It uses a formula such as `hash(key) % numberOfShards` to select a shard.

### Q5. What is the problem with modulus-based sharding?

Changing the shard count can change the calculated destination for many existing records, requiring substantial redistribution.

### Q6. How does consistent hashing work?

It maps shard identifiers and data keys to a hash ring. Each key is assigned to the first shard clockwise from its position.

### Q7. Why is consistent hashing useful?

It minimizes redistribution when the shard membership changes because only a subset of keys generally receives a new destination.

### Q8. Does consistent hashing automatically move database records?

No. It determines the new destination. The application or migration system must move the affected records safely.

### Q9. What are virtual nodes?

They are multiple logical positions on the hash ring associated with one physical shard. They help improve distribution and load balancing.

### Q10. Can consistent hashing eliminate hotspots?

No. It can improve placement balance, but hot keys, uneven traffic, and unequal record sizes can still create hotspots.

### Q11. Can sharding and replication be used together?

Yes. Each shard can have replicas to support read scaling and availability.

### Q12. Does Spring Data JPA automatically support sharding?

Not by itself. Shard selection and routing must be implemented or provided through a suitable sharding solution.

---

## 14. Final Summary

Remember these core concepts:

- **Sharding:** splits a dataset across multiple database servers.
- **Sharding key:** determines the destination of a record.
- **Range-based sharding:** distributes records according to key ranges.
- **Modulus-based sharding:** uses `hash(key) % N`.
- **Consistent hashing:** uses a hash ring to minimize redistribution when shards are added or removed.
- **Virtual nodes:** improve distribution across physical shards.
- **Replication:** keeps copies of data and complements sharding.
- **Re-sharding:** requires controlled migration and routing updates; it is not automatic.

**Interview takeaway:** Modulus-based sharding is simple but can require extensive redistribution when the shard count changes. Consistent hashing reduces the number of records that need to move by limiting ownership changes to the affected portions of the hash ring.