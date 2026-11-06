# Cache Read Strategies in System Design

Cache Read Strategies define how an application retrieves data from a cache and handles cache misses.

The two commonly used strategies are:

1. Cache-Aside (Lazy Loading)
2. Read-Through

---

## 1. Cache-Aside (Lazy Loading)

In Cache-Aside, the application itself manages the cache.

The application first checks the cache. If the requested data is not available, the application fetches it from the database, stores it in the cache, and returns it to the client.

### Workflow

**Cache Hit:**

1. Client requests data.
2. Application checks the cache.
3. Cache contains the requested data.
4. Application returns the cached data to the client.
5. Database is not queried.

**Cache Miss:**

1. Client requests data.
2. Application checks the cache.
3. Cache does not contain the requested data.
4. Application fetches the data from the database.
5. Application stores the fetched data in the cache.
6. Application returns the data to the client.

### Cache-Aside Flow

```text
             Client
                |
                v
         Application Server
                |
                v
          Check Cache
                |
          +-----+-----+
          |           |
       Cache Hit   Cache Miss
          |           |
          v           v
      Return Data   Query Database
                      |
                      v
                 Fetch Data
                      |
                      v
                 Update Cache
                      |
                      v
                  Return Data
```

### Example

Suppose a client requests product details:

`GET /products/101`

**First request:**

- Product is not available in Redis.
- Application queries the database.
- Database returns product details.
- Application stores the product in Redis.
- Application returns the response.

**Second request:**

- Application checks Redis.
- Product is already available.
- Application returns the cached data.
- Database is not queried.

### Java / Spring Boot-style Example

```java
public Product getProduct(Long productId) {

    // 1. Check cache
    Product product = redis.get("product:" + productId);

    if (product != null) {
        return product; // Cache Hit
    }

    // 2. Cache Miss: Fetch from database
    product = productRepository.findById(productId)
            .orElseThrow();

    // 3. Store in cache
    redis.set("product:" + productId, product);

    // 4. Return data
    return product;
}
```

### Advantages

- Simple and widely used caching strategy.
- Application has complete control over cache operations.
- Data is loaded only when requested.
- Unused data does not unnecessarily occupy cache memory.

### Disadvantages

- Application must implement cache lookup and miss-handling logic.
- Application code becomes responsible for cache management.
- Cache and database can become inconsistent if invalidation is not handled properly.

### Important Point

Cache-Aside is also called **Lazy Loading** because data is loaded into the cache only when it is requested.

---

## 2. Read-Through

In Read-Through, the cache itself is responsible for loading missing data from the database.

The application always requests data through the cache.

When a cache miss occurs, the cache invokes a configured loader to fetch the data from the database, stores it, and returns it to the application.

### Workflow

**Cache Hit:**

1. Application requests data from the cache.
2. Cache finds the requested data.
3. Cache returns the data to the application.
4. Application returns it to the client.
5. Database is not queried.

**Cache Miss:**

1. Application requests data from the cache.
2. Cache does not contain the requested data.
3. Cache invokes its configured data loader.
4. Loader fetches the data from the database.
5. Cache stores the fetched data.
6. Cache returns the data to the application.
7. Application returns it to the client.

### Read-Through Flow

```text
             Client
                |
                v
         Application Server
                |
                v
          Read From Cache
                |
          +-----+-----+
          |           |
       Cache Hit   Cache Miss
          |           |
          v           v
      Return Data  Cache Invokes
                    Data Loader
                        |
                        v
                   Query Database
                        |
                        v
                    Fetch Data
                        |
                        v
                  Update Cache
                        |
                        v
                   Return Data
```

### Advantages

- Application does not need to implement cache-miss loading logic.
- Cache loading responsibility is centralized.
- Application code becomes simpler.
- Frequently accessed data can be served directly from the cache.

### Disadvantages

- Requires a cache library or integration that supports a configured loader.
- Application has less direct control over cache loading.
- Cache loader failures must be handled properly.

### Important Point

In Read-Through, the cache is responsible for fetching missing data from the underlying data source.

---

## 3. Cache-Aside vs Read-Through

The fundamental difference is **who handles a cache miss**.

| Feature                | Cache-Aside            | Read-Through              |
|------------------------|------------------------|---------------------------|
| Cache lookup           | Application            | Application through cache |
| Cache miss handling    | Application            | Cache                     |
| Database fetching      | Application            | Cache loader              |
| Cache population       | Application            | Cache                     |
| Application complexity | Higher                 | Lower                     |
| Cache abstraction      | Application manages it | Cache manages loading     |

### Fundamental Difference

**Cache-Aside:**

```text
Application -> Cache

On Cache Miss:
Application -> Database
Application -> Cache
```

**Read-Through:**

```text
Application -> Cache

On Cache Miss:
Cache -> Database
Cache stores data
Cache -> Application
```

---

## 4. Can Read Strategies and Write Strategies Be Combined?

Yes. Read strategies and write strategies are independent design decisions.

A system can use one strategy for reading and another strategy for writing.

Examples:

| Read Strategy | Write Strategy |
|---------------|----------------|
| Cache-Aside   | Write-Through  |
| Cache-Aside   | Write-Around   |
| Read-Through  | Write-Through  |
| Read-Through  | Write-Back     |

The selection depends on application requirements, consistency, performance, and data durability.

---

## 5. Interview Questions

**Q1. What is Cache-Aside?**

Cache-Aside is a caching strategy in which the application checks the cache first. If the data is missing, the application fetches it from the database and populates the cache.

**Q2. What is Read-Through?**

Read-Through is a caching strategy in which the cache itself fetches missing data from the database using a configured loader.

**Q3. What is the main difference between Cache-Aside and Read-Through?**

The main difference is cache-miss handling.

- Cache-Aside: Application handles cache misses.
- Read-Through: Cache handles cache misses.

**Q4. Why is Cache-Aside also called Lazy Loading?**

Because data is loaded into the cache only when it is requested, rather than loading all database records in advance.

**Q5. Does Redis automatically provide Read-Through caching?**

No. Redis is commonly used with Cache-Aside, where the application explicitly handles cache misses. Read-Through requires an additional caching layer or integration that supports data loaders.

---

## 6. Quick Revision

| Concept      | Responsibility                               |
|--------------|----------------------------------------------|
| Cache-Aside  | Application manages cache lookup and loading |
| Read-Through | Cache manages loading missing data           |
| Cache Hit    | Requested data is available in cache         |
| Cache Miss   | Requested data is not available in cache     |
| Lazy Loading | Load data only when requested                |

**Remember:**

- Cache-Aside = Application fetches missing data.
- Read-Through = Cache fetches missing data.