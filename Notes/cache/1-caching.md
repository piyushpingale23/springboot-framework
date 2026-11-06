# Caching in System Design

## 1. What is Caching?

Caching is a technique of storing frequently accessed data in a temporary storage location so that it can be retrieved faster instead of fetching it repeatedly from the original source.

### Why do we need caching?

Consider an e-commerce application where users frequently access the same product details.

Without caching:
- Every request may trigger a database query.
- Database load increases.
- Response time increases.
- Application scalability decreases.

With caching:
- Frequently accessed data is stored in a faster storage location.
- Subsequent requests retrieve data from the cache.
- Database queries are reduced.
- Response time improves.

### Architecture: Without Caching

```text
        10,000 User Requests
                 |
                 v
          Application Server
                 |
                 v
              Database
         (Repeated Queries)
```

### Architecture: With Caching

```text
        10,000 User Requests
                 |
                 v
          Application Server
                 |
                 v
              Cache
                 |
          Cache Hit / Miss
                 |
          +------+------+
          |             |
        Hit            Miss
          |             |
          v             v
       Return        Database
       Cached           |
        Data            v
                     Update Cache
```

---

# 2. Types of Caching

There are four commonly used caching locations:

1. Browser Caching
2. CDN (Content Delivery Network)
3. Local Caching
4. Global Caching (Centralized / Distributed Cache)

## 2.1 Browser Caching (Client-Side Caching)

Browser caching means storing frequently accessed resources on the user's browser itself.

When a user visits a website, the browser downloads resources such as images, CSS, JavaScript, and fonts.

The browser can store these resources locally and reuse them during subsequent visits instead of downloading them again.

### What can be cached in the browser?

**1. Static Resources**

- HTML files
- CSS files
- JavaScript files
- Images
- Fonts
- Icons

**2. API Responses**

Some API responses can also be cached using HTTP caching mechanisms, depending on the response headers and caching policy.

**3. Browser Storage**

Browsers provide the following storage mechanisms:

- Local Storage
- Session Storage
- IndexedDB
- Cache Storage (used by Service Workers)

These are browser-side storage mechanisms, but they are not all equivalent to HTTP browser caching.

### HTTP Caching Headers

The server controls browser caching behavior using HTTP response headers.

Example:

```http
Cache-Control: max-age=3600
```

This allows the browser to reuse the cached response for 3600 seconds (1 hour), subject to HTTP caching rules.

Another example:

```http
Cache-Control: no-cache
```

The browser must validate the response with the server before reusing it.

**Important:** `no-cache` does not necessarily mean that the response cannot be stored.

### DNS Caching

DNS caching is different from browser local storage.

When a user enters:

```text
www.amazon.com
```

The browser needs to resolve the domain name into an IP address.

DNS responses can be cached at multiple levels:

1. Browser DNS Cache
2. Operating System DNS Cache
3. DNS Resolver Cache

This avoids repeatedly performing DNS lookups.

```text
www.amazon.com
       |
       v
 DNS Resolution
       |
       v
    IP Address
       |
       v
 DNS Result Cached
       |
       v
 Reused until TTL expires
```

**Important:** DNS caching is not the same as Local Storage.

---

## 2.2 CDN (Content Delivery Network)

CDN stands for Content Delivery Network.

A CDN is a geographically distributed network of servers that stores cached copies of content closer to users.

A CDN is not just one storage server. It is a network of caching servers distributed across different geographical locations.

### Why do we need CDN?

Suppose our application is hosted in Mumbai, India.

Users are accessing the application from:

- Mumbai, India
- London, UK
- New York, USA
- Tokyo, Japan

Without a CDN, users may need to download static resources from the origin server in Mumbai.

This increases network latency, especially for geographically distant users.

A CDN stores copies of static resources at geographically distributed edge servers.

### How does CDN work?

Consider an image:

```text
https://example.com/images/iphone.jpg
```

1. A user from London requests the image.
2. The request reaches a nearby CDN edge server.
3. If the image is available in the CDN cache, it returns the image directly.
4. If the image is not available, the CDN fetches it from the origin server.
5. The CDN stores a copy according to its caching policy.
6. Subsequent requests can be served from the CDN.

### CDN Architecture

```text
                 User
                  |
                  v
          Nearby CDN Edge Server
                  |
             Cache Check
                  |
          +-------+-------+
          |               |
       Cache Hit       Cache Miss
          |               |
          v               v
      Return Image    Origin Server
                          |
                          v
                     Store in CDN
                          |
                          v
                      Return Image
```

### Cache Hit

When requested data is already available in the cache.

### Cache Miss

When requested data is not available in the cache.

The CDN fetches the content from the origin server.

### What can be cached in CDN?

Mostly static resources:

- Images
- Videos
- CSS
- JavaScript
- Fonts
- Downloadable files

CDNs can also cache certain API responses and dynamic content when caching rules allow it.

### Popular CDN Providers

- Cloudflare
- Amazon CloudFront
- Akamai
- Fastly

### Advantages of CDN

1. Reduces network latency.
2. Reduces load on origin servers.
3. Improves content delivery speed.
4. Improves scalability.
5. Provides geographically distributed content delivery.

---

## 2.3 Local Caching (Application-Level Caching)

Local caching means storing frequently accessed data directly inside the application server's memory.

For example, in a Java Spring Boot application, we can use:

- Java HashMap
- Caffeine Cache
- Ehcache
- Spring Cache abstraction with a local cache provider

### Why do we need Local Caching?

Suppose we have a Spring Boot application.

Whenever a user requests product details, the application fetches the product from the database.

```text
Client
  |
  v
Spring Boot Application
  |
  v
Database
```

If the same product is requested repeatedly, we don't necessarily need to query the database every time.

Instead, we can store the product information in the application server's memory.

### How does Local Caching work?

Consider this API:

```http
GET /products/101
```

**First Request:**

1. User requests product 101.
2. Application checks its local cache.
3. Product is not available in the cache.
4. Application fetches product 101 from the database.
5. Application stores the product in local memory.
6. Application returns the response.

**Second Request:**

1. User requests product 101 again.
2. Application checks its local cache.
3. Product is available.
4. Application returns cached data without querying the database.

### Local Caching Architecture

```text
              Client
                |
                v
        Spring Boot Application
                |
                v
          Local Cache
          (Caffeine)
                |
          Cache Hit / Miss
                |
          +-----+-----+
          |           |
         Hit         Miss
          |           |
          v           v
       Return      Database
       Cached          |
        Data           v
                   Update Cache
```

### Spring Boot Example

Using Spring Cache with Caffeine:

```java
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {

        System.out.println("Fetching from database");

        return productRepository.findById(id)
                .orElseThrow();
    }
}
```

### Execution

| Request                   | Result         |
|---------------------------|----------------|
| First request for ID 101  | Database query |
| Second request for ID 101 | Local cache    |
| Third request for ID 101  | Local cache    |

The method executes only when the requested key is not available in the cache, assuming normal cache operation.

### Problem with Local Caching in Distributed Applications

Suppose we have three application instances behind a Load Balancer.

- Application Server 1
- Application Server 2
- Application Server 3

Each application server maintains its own local cache.

```text
                 Client
                   |
                   v
              Load Balancer
                   |
          +--------+--------+
          |        |        |
          v        v        v
        App 1    App 2    App 3
          |        |        |
       Cache 1  Cache 2  Cache 3
```

Consider this scenario:

1. User requests product 101.
2. Load Balancer sends the request to Server 1.
3. Server 1 fetches product 101 from the database.
4. Server 1 stores it in its local cache.
5. The next request goes to Server 2.
6. Server 2 does not have product 101 in its local cache.
7. Server 2 queries the database again.

**Why does this happen?**

Because the local cache of Server 1 is not accessible to Server 2.

### Problems of Local Caching

1. Cache duplication across application servers.
2. Cache inconsistency between application servers.
3. Each application instance manages its own cache.
4. Updating one instance's cache does not automatically update other instances.

### Versioning + Caching

Versioning is a cache invalidation technique, especially useful for static resources.

Suppose the browser has cached:

```text
/css/style.css
```

Now we update the CSS file on the server.

The browser may still reuse the old cached file.

How can we force the browser to request the updated file?

One solution is versioning.

Instead of:

```text
/css/style.css
```

Use:

```text
/css/style-v1.css
```

After updating the CSS:

```text
/css/style-v2.css
```

Because the URL changes, the browser treats it as a new resource and downloads it.

### Content Hash Versioning

A common production approach is content-hash versioning.

```text
/css/style.a83f91.css
/css/style.b74e21.css
```

The filename changes when the file content changes.

**Important:** Versioning is a cache invalidation technique, not a separate type of cache.

---

## 2.4 Global Caching (Centralized / Distributed Caching)

Global caching means storing frequently accessed data in a shared cache that can be accessed by multiple application servers.

A common example is Redis.

Another option is Memcached.

### Why do we need Global Caching?

As discussed earlier, local caching has limitations when multiple application instances are running.

Each application server maintains its own independent cache.

This creates:

1. Cache duplication.
2. Cache inconsistency.
3. Repeated database queries when requests reach different instances.

Instead of maintaining independent copies, we can introduce a centralized Redis cache.

### Global Caching Architecture

```text
                  Client
                    |
                    v
               Load Balancer
                    |
           +--------+--------+
           |        |        |
           v        v        v
         App 1    App 2    App 3
           |        |        |
           +--------+--------+
                    |
                    v
                Redis Cache
                    |
                Cache Miss
                    |
                    v
                 Database
```

### How does Global Caching work?

Suppose a user requests product 101.

**First Request:**

1. Request reaches Application Server 1.
2. Server 1 checks Redis.
3. Product 101 is not available in Redis.
4. Server 1 fetches product 101 from the database.
5. Server 1 stores the product in Redis.
6. Server 1 returns the response.

**Second Request:**

1. Request reaches Application Server 2.
2. Server 2 checks Redis.
3. Product 101 is already available.
4. Redis returns the cached product.
5. Server 2 returns the response.

**Third Request:**

1. Request reaches Application Server 3.
2. Server 3 checks Redis.
3. Product 101 is already available.
4. Redis returns the cached product.

All application servers access the same Redis cache.

### Local Cache vs Global Cache

| Parameter        | Local Cache                       | Global Cache                   |
|------------------|-----------------------------------|--------------------------------|
| Storage          | Application memory                | Separate cache infrastructure  |
| Sharing          | Single application instance       | Multiple application instances |
| Example          | Caffeine                          | Redis                          |
| Network call     | Usually not required              | Required                       |
| Latency          | Very low                          | Higher than local cache        |
| Data consistency | Difficult across instances        | Easier to coordinate           |
| Infrastructure   | No separate cache server required | Separate cache infrastructure  |
| Failure impact   | Usually limited to one instance   | Can affect multiple instances  |

**Important:** Global caching does not automatically guarantee consistency. We still need proper invalidation, expiration, and update strategies.

---

# 3. Problems of Caching

There are three major problems associated with caching:

1. Cache is not the original source of data.
2. Cache and database can get out of sync.
3. Cache can run out of memory.

## 3.1 Cache is Not the Original Source of Truth

A cache is only a temporary copy of the original data.

The original data is generally stored in a persistent database such as:

- MySQL
- PostgreSQL
- MongoDB

A cache such as Redis stores a copy of that data to provide faster access.

### Example

Suppose an e-commerce application contains:

| Product ID | Product Name | Price   |
|------------|--------------|---------|
| 101        | iPhone       | ₹80,000 |
| 102        | Samsung      | ₹60,000 |

When a user requests product 101, the application fetches it from the database and stores it in Redis.

```text
       Database (PostgreSQL)
       Original Source of Truth
                 |
                 | Copy Data
                 v
             Redis Cache
             Temporary Copy
```

Now imagine Redis crashes or its cached data gets deleted.

What happens?

1. Cached product information is lost.
2. Original product information still exists in PostgreSQL.
3. Application fetches the product again from PostgreSQL.
4. Redis can be populated again.

**Important:** Redis supports persistence and replication, but that does not automatically make it the authoritative source of business data.

---

## 3.2 Cache and Database Can Get Out of Sync

This is one of the most important problems in caching.

Cache and database can contain different values for the same record.

This happens when the database gets updated but the corresponding cached data does not.

### Example

Suppose an e-commerce application contains:

| Storage     | Product Price |
|-------------|---------------|
| Database    | ₹80,000       |
| Redis Cache | ₹80,000       |

Everything is synchronized.

Now the business team decides to increase the product price from ₹80,000 to ₹90,000.

The application updates the database but does not update or delete the cached value.

Now:

| Storage     | Product Price |
|-------------|---------------|
| Database    | ₹90,000       |
| Redis Cache | ₹80,000       |

This is known as **Cache Inconsistency**.

A user might still see the old price of ₹80,000 even though the actual price is ₹90,000.

### Solution: Cache Invalidation

Cache invalidation means removing or marking cached data as invalid when the original data changes, so that stale data is not reused.

### Cache Invalidation Flow

1. Update product price in the database.
2. Delete the corresponding Redis cache entry.
3. The next user requests product 101.
4. Redis does not contain the product.
5. Application fetches the latest product from the database.
6. Application stores the latest product in Redis.

```text
       Update Database
       Product 101 = ₹90,000
                |
                v
       Invalidate Redis Key
         Delete product:101
                |
                v
       User Requests Product 101
                |
                v
          Redis Cache
                |
                v
            Cache Miss
                |
                v
          Fetch from DB
                |
                v
         Product = ₹90,000
                |
                v
          Update Redis
```

### Spring Boot Example

Using `@CacheEvict`:

```java
@CacheEvict(value = "products", key = "#id")
public void updateProduct(Long id, Product product) {

    Product existing = productRepository.findById(id)
            .orElseThrow();

    existing.setPrice(product.getPrice());

    productRepository.save(existing);
}
```

Here, `@CacheEvict` removes the cached product after the method successfully executes.

### Other Cache Invalidation Techniques

**1. TTL (Time To Live)**

Cached data automatically expires after a specified duration.

**2. Write-Through**

Updates are propagated to the database and cache together through the caching strategy.

**3. Cache-Aside with Invalidation**

Delete the cached entry when database data changes.

**4. Event-Driven Invalidation**

Publish an event when data changes and let cache consumers invalidate their entries.

**Important:** Database updates and cache deletion can fail independently. For critical applications, transactional outbox, event-driven invalidation, and retries help handle this problem.

---

## 3.3 Cache Uses RAM, So It Can Run Out of Memory (OOM)

Most caching systems, including Redis and Caffeine, primarily store cached data in memory (RAM).

RAM is limited.

Suppose your Redis server has 4 GB of available memory.

Your application starts caching:

| Data                 | Memory |
|----------------------|--------|
| Product Information  | 1 GB   |
| Customer Information | 1 GB   |
| Order Information    | 1 GB   |
| Other Information    | 1 GB   |
| Total                | 4 GB   |

Now the cache has consumed approximately 4 GB.

What happens when the application tries to store another 500 MB?

It depends on the cache's memory configuration and eviction policy.

It may:

- Evict existing cached keys.
- Reject new writes.
- Encounter memory pressure or an OOM condition.

### Solution: Cache Eviction

Cache eviction means removing existing cached entries to free up memory when the cache reaches its configured memory limit.

Instead of allowing the cache to grow indefinitely, we define an eviction policy.

### Popular Cache Eviction Policies

| Policy | Working                                                             |
|--------|---------------------------------------------------------------------|
| LRU    | Least Recently Used – removes the least recently accessed data.     |
| LFU    | Least Frequently Used – removes the least frequently accessed data. |
| FIFO   | First In First Out – removes the oldest inserted data.              |
| Random | Removes a randomly selected entry.                                  |

### Example: LRU (Least Recently Used)

Suppose Redis has space for only three products.

Initially:

```text
Cache:
Product 101
Product 102
Product 103
```

Now users access:

- Product 101
- Product 102

Product 103 becomes the least recently used.

A new request arrives for product 104.

Using LRU:

1. Identify the least recently used entry.
2. Remove product 103.
3. Insert product 104.

```text
Before:

+-------------+
| Product 101 |
| Product 102 |
| Product 103 |
+-------------+

Product 101 accessed
Product 102 accessed

Product 103 is least recently used.

New Product 104 arrives.

After:

+-------------+
| Product 101 |
| Product 102 |
| Product 104 |
+-------------+

Product 103 is evicted.
```

### Redis Eviction Policies

Redis supports several eviction policies:

- `allkeys-lru`
- `allkeys-lfu`
- `volatile-lru`
- `volatile-lfu`
- `allkeys-random`
- `volatile-random`
- `volatile-ttl`
- `noeviction`

**Note:** FIFO is a general eviction strategy, but is not one of Redis's standard named `maxmemory-policy` options.

---

# 4. Summary: Problems of Caching

| Problem                    | Why does it happen?                    | Solution                                         |
|----------------------------|----------------------------------------|--------------------------------------------------|
| Cache is not original data | Cache stores a copy of database data   | Database remains source of truth                 |
| Cache and DB out of sync   | Database changes but cache remains old | Cache Invalidation, TTL, synchronization         |
| Cache runs out of memory   | RAM is limited                         | Cache Eviction, memory limits, capacity planning |

---

# 5. Additional Caching Problems to Learn

Beyond the three problems discussed above, there are three important challenges in distributed caching.

## 5.1 Cache Stampede

Many requests simultaneously try to fetch the same expired or missing data.

This puts excessive load on the database.

## 5.2 Cache Penetration

Requests repeatedly ask for data that does not exist in the database, causing repeated cache misses.

## 5.3 Cache Avalanche

Many cache entries expire simultaneously, resulting in a sudden increase in database requests.

These are important system design interview topics.

---

# 6. Important Concepts to Remember

1. Cache is a temporary copy of original data.
2. Database generally remains the source of truth.
3. Browser cache stores resources on the user's device.
4. CDN stores content on geographically distributed edge servers.
5. Local cache stores data inside an individual application instance.
6. Global cache provides shared caching across multiple application instances.
7. Cache invalidation handles stale cached data.
8. Cache eviction frees memory by removing existing cache entries.
9. TTL defines how long cached data remains valid.
10. Versioning helps clients request updated static resources.

---
