# Cache Penetration

## 1. What is Cache Penetration?

Cache Penetration occurs when an application receives repeated requests for data that does not exist in the database.

Since the requested data is not available in the cache, every request results in a database query.

This can overload the database.

It is also known as Cache Miss for Non-Existent Data.

### Example

Suppose the database contains:

| Product ID | Product Name |
|------------|--------------|
| 101        | Laptop       |
| 102        | Mobile       |
| 103        | Keyboard     |

A client repeatedly requests:

```http
GET /products/999
```

Product ID 999 does not exist.

Request flow:

1. Application checks Redis.
2. Redis returns Cache Miss.
3. Application queries the database.
4. Database returns Product Not Found.
5. Nothing is cached.
6. The next request repeats the same process.

If thousands of requests arrive for the same non-existent product, all of them may reach the database.

## 2. How to Prevent Cache Penetration?

### 1. Negative Caching (Cache Null)

Instead of caching only existing records, cache the fact that a record does not exist.

Example:

```text
product:999 = NOT_FOUND
TTL = 60 seconds
```

Request flow:

1. Application checks Redis.
2. Redis returns NOT_FOUND.
3. Application returns 404.
4. Database is not queried again.

**Advantages:**

- Prevents repeated database queries for missing records.
- Simple to implement.
- Reduces database load.

**Limitation:**

- If a record is created after NOT_FOUND is cached, the application may temporarily return 404 until the negative cache entry expires or is invalidated.

### 2. Bloom Filter

A Bloom Filter is a space-efficient probabilistic data structure used to check whether an element might exist in a set.

It provides two possible results:

- Definitely not present: The element does not exist.
- Possibly present: The element may exist; check the cache/database.

A standard Bloom Filter can produce false positives but does not produce false negatives for inserted elements.

Example:

Database contains:

```text
101, 102, 103
```

Request arrives for product ID 999.

1. Application checks the Bloom Filter.
2. Bloom Filter says 999 is definitely not present.
3. Application returns 404.
4. Redis and database are not queried.

**Advantages:**

- Reduces unnecessary cache and database lookups.
- Memory efficient.
- Useful for large datasets.

**Limitations:**

- False positives are possible.
- Must be updated when records are added.
- Standard Bloom Filters do not support straightforward deletion.

### 3. Input Validation

Validate incoming requests before querying the cache or database.

Example:

```http
GET /products/abc
```

If product IDs must be numeric, reject the request immediately.

```java
@GetMapping("/products/{id}")
public Product getProduct(@PathVariable Long id) {
    return productService.getProduct(id);
}
```

Spring automatically rejects a non-numeric path value when it cannot convert it to Long.

**Advantages:**

- Rejects malformed requests early.
- Avoids unnecessary processing.

**Limitation:**

- Does not prevent requests for valid-looking but non-existent IDs such as 999.

## 3. Comparison of Prevention Techniques

| Technique        | How it helps                        | Limitation                                 |
|------------------|-------------------------------------|--------------------------------------------|
| Negative Caching | Caches NOT_FOUND results            | Can temporarily hide newly created data    |
| Bloom Filter     | Rejects definitely absent IDs early | False positives possible                   |
| Input Validation | Rejects malformed requests          | Does not reject valid but non-existent IDs |

## 4. Cache Stampede vs Cache Penetration

| Cache Stampede                                         | Cache Penetration                          |
|--------------------------------------------------------|--------------------------------------------|
| Many requests for the same expired/missing cache entry | Requests for non-existent data             |
| Data usually exists in DB                              | Data does not exist in DB                  |
| Many requests hit DB simultaneously                    | Repeated requests hit DB                   |
| Locking, TTL jitter, background refresh                | Negative caching, Bloom Filter, validation |

### Remember

- Cache Stampede: Data exists, but many requests try to load it at once.
- Cache Penetration: Data does not exist, but requests keep asking for it.
