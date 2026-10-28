# Load Balancing Strategies / Algorithms

## 1. What is Load Balancing?

**Load Balancing** is the process of distributing incoming requests/traffic across multiple instances of a service.

### Example

```text
                         Load Balancer
                              |
                +-------------+-------------+
                |             |             |
                v             v             v
              O1             O2            O3
           Instance 1      Instance 2    Instance 3
```

Instead of sending all requests to one instance:

```text
1000 requests
     |
     v
    O1          <- overloaded
```

the load balancer distributes them:

```text
1000 requests
     |
     v
+----+----+----+
|         |    |
O1        O2   O3
330       330  340
```

### Main goals

* Distribute traffic
* Avoid overloading one instance
* Improve availability
* Improve scalability
* Improve response time
* Provide fail over when an instance becomes unavailable

---

# 2. Round Robin

**Round Robin** distributes requests sequentially across available instances.

### Example

Instances:

```text
O1
O2
O3
```

Requests:

```text
Request 1 → O1
Request 2 → O2
Request 3 → O3
Request 4 → O1
Request 5 → O2
Request 6 → O3
```

### Advantages

* Very simple
* Easy to implement
* Good when all instances have similar capacity

### Disadvantages

It does not consider:

* CPU usage
* Memory usage
* Active connections
* Different instance capacities

For example:

```text
O1 → 8 CPU cores
O2 → 8 CPU cores
O3 → 2 CPU cores
```

Round Robin still gives approximately:

```text
O1 → 33%
O2 → 33%
O3 → 33%
```

which may overload O3.

### Best suited for

Instances with approximately equal capacity and similar workloads.

### Interview one-liner

> Round Robin distributes requests sequentially across backend instances without considering their current load.

---

# 3. Weighted Round Robin

**Weighted Round Robin** assigns a weight to each instance based on its relative capacity.

Higher weight → more traffic.

### Example

```text
O1 → Weight 4
O2 → Weight 2
O3 → Weight 1
```

Total weight:

```text
4 + 2 + 1 = 7
```

Approximate traffic:

```text
O1 → 4/7 ≈ 57%
O2 → 2/7 ≈ 29%
O3 → 1/7 ≈ 14%
```

Example sequence:

```text
O1 → O1 → O1 → O1
O2 → O2
O3
```

The exact scheduling sequence depends on the implementation.

### What does the weight represent?

Weight usually represents the **relative capacity** of an instance.

It can be configured based on:

* CPU cores
* Memory

For example:

```text
O1 → 8 CPU cores → weight 4
O2 → 4 CPU cores → weight 2
O3 → 2 CPU cores → weight 1
```

### Important

Weight does **not normally mean current CPU utilization**.

```text
Weight -> "How powerful is this instance?"

CPU Utilization -> "How busy is this instance right now?"
```

### Advantages

* Supports different instance capacities

> Weighted Round Robin distributes traffic according to configured weights, giving more requests to instances with greater capacity.

---

# 4. Least Connections

**Least Connections** sends a new request to the instance currently having the **fewest active connections**.

### Example

```text
O1 → 5 active connections
O2 → 2 active connections
O3 → 8 active connections
```

New request:

```text
              New Request
                   |
                   v
             Load Balancer
                   |
                   v
                  O2
```

Because:

```text
O2 = 2
```

is the lowest.

After accepting the new connection:

```text
O1 → 5
O2 → 3
O3 → 8
```

### What is an active connection?

An active connection is an **open network connection between a client and a backend instance**.

Example:

```text
Client A ───── Connection 1 ─────┐
Client B ───── Connection 2 ─────┤
Client C ───── Connection 3 ─────┤──> O1
Client D ───── Connection 4 ─────┤
Client E ───── Connection 5 ─────┘
```

Therefore:

```text
O1 → 5 active connections
```

means O1 currently has 5 open connections.

### Important: Connection ≠ Request

One connection can carry multiple requests.

```text
Connection 1
   |
   +── Request 1
   +── Request 2
   +── Request 3
```

Therefore:

```text
5 active connections
```

does **not** necessarily mean:

```text
5 requests
```

It means:

```text
5 open connections
```

### Why choose the least connections?

Suppose:

```text
O1 → 2 connections
O2 → 5 connections
O3 → 10 connections
```

Sending another request to O3 would make the busiest server even busier.

Instead:

```text
New Request → O1
```

The goal is to distribute work rather than send more traffic to the busiest instance.

### Advantages

* Considers current connection load

> Least Connections sends a new request to the backend instance with the fewest active connections.

---

# 5. Weighted Least Connections

**Weighted Least Connections** combines:

* Current active connections
* Instance capacity/weight

It tries to select an instance based on both its current connection load and its relative capacity.

### Example

```text
O1 → Weight 4 → 8 connections
O2 → Weight 2 → 3 connections
O3 → Weight 1 → 2 connections
```

A simple least-connection algorithm sees:

```text
O3 → 2 connections
```

and may choose O3.

But O3 is the smallest-capacity server.

A weighted approach considers that O1 has much greater capacity.

### Why use it?

Useful when instances have different capacities and connection counts vary.

> Weighted Least Connections considers both the number of active connections and the relative capacity of backend instances.

---

# 6. Random

The load balancer randomly selects an available instance.

Example:

```text
O1
O2
O3
```

Requests might be distributed as:

```text
R1 → O2
R2 → O1
R3 → O3
R4 → O3
R5 → O1
R6 → O2
```

There is no fixed sequence.

### Advantages

* Low decision-making overhead
* No need to track connections

### Disadvantages

Short-term distribution may be uneven.

Example:

```text
O1 → 2 requests
O2 → 3 requests
O3 → 10 requests
```

Random selection does not care that O3 currently has more requests.

> Random load balancing randomly selects an available backend instance for each request.

---

# 7. Weighted Random

Weighted Random combines random selection with instance weights.

Example:

```text
O1 → Weight 5
O2 → Weight 3
O3 → Weight 2
```

Total:

```text
10
```

Approximate traffic:

```text
O1 → 50%
O2 → 30%
O3 → 20%
```

> Weighted Random randomly distributes traffic while giving higher-capacity instances a higher probability of selection.

---

# 8. IP Hash

**IP Hash** uses the client's IP address to determine the backend instance.

Conceptually:

```text
Client IP
    |
    v
Hash(Client IP)
    |
    v
Instance selection
```

Example:

```text
Client A
IP = 192.168.1.10
        |
        v
      Hash
        |
        v
       O1
```

The next request from the same IP will generally map to the same instance.

```text
192.168.1.10
      |
     Hash
      |
      v
     O1
```

### Problem with IP Hash

Multiple users can share the same public IP.

Example:

```text
User A ─┐
User B ─┤
User C ─┤
        ├── NAT ──> Public IP ──> Load Balancer
User D ─┤
User E ─┘
```

All users may appear as:

```text
203.0.113.10
```

Therefore:

```text
Hash(203.0.113.10) → O1
```

Many users could be sent to O1.

> IP Hash hashes the client's IP address to consistently route that client toward the same backend instance.

---

# 9. Sticky Sessions / Session Affinity

Sticky Session means the load balancer tries to keep a client/session connected to the **same backend instance** for subsequent requests.

Example:

```text
Client
   |
   v
Load Balancer
   |
   v
  O1
```

The load balancer may provide a cookie:

```text
server=O1
```

Next request:

```text
Client
   |
   | Cookie: server=O1
   v
Load Balancer
   |
   v
  O1
```

### Why use Sticky Sessions?

Suppose session information is stored locally:

```text
O1
 |
 +-- User Session
```

If the next request goes to O2:

```text
Client → O2

O2 doesn't have the session
```

Sticky sessions help keep the client on O1.

### Problem

If O1 goes down:

```text
Client → O1
          X
        DOWN
```

The session may be lost unless the session is stored somewhere shared.

> Sticky sessions maintain session affinity by repeatedly routing a client/session to the same backend instance.

---

# 10. Resource-Based / Least Load

Resource-based load balancing selects an instance based on its current resource utilization.

It may consider:

```text
CPU
Memory
Network
Active connections
```

Example:

```text
O1 → CPU 40%
O2 → CPU 80%
O3 → CPU 25%
```

A resource-aware load balancer may select:

```text
O3
```

because it currently has the lowest load.

> Resource-based load balancing selects instances based on their current resource utilization such as CPU, memory.

---

# 11. Consistent Hashing

**Consistent Hashing** maps keys and instances onto a logical **hash ring**.

It is particularly useful when we need a stable mapping such as:

### Basic flow

```text
Key
 |
 v
Hash Function
 |
 v
Position on Hash Ring
 |
 v
Next available instance
```

Example:

```text
hash(User 101) = 120
```

Suppose:

```text
O1 = 50
O2 = 170
O3 = 280
```

The next instance clockwise after position 120 is O2.

Therefore:

```text
User 101 → O2
```

---

### Hash Ring

Conceptually:

```text
                  O1
                  50
                   |
             +-----+-----+
           /               \
          /                 \
         /                   \
      O3                       O2
     280                      170
          \                 /
           \               /
             +-----------+
```

The ring wraps around:

```text
360 → 0
```

---

## 11.1 What happens when an instance goes down?

Suppose:

```text
O1
O2
O3
```

and:

```text
O2 → DOWN
```

O2 is removed from the ring.

Keys that previously mapped to O2 are reassigned to the next available instance.

For example:

```text
Before:

User A → O1
User B → O2
User C → O3
User D → O2
```

After O2 goes down:

```text
User A → O1    unchanged
User B → O3    changed
User C → O3    unchanged
User D → O3    changed
```

The important point is:

> **Only keys belonging to the failed instance's portion of the ring need to move.**

Other keys generally remain unchanged.

---

## 11.2 What happens when an instance is added?

Suppose:

```text
O1
O2
O3
```

Add:

```text
O4
```

O4 is placed at a position on the ring.

Only keys around O4's new region may move.

```text
Before:

O1 -------- O2 -------- O3


After:

O1 -------- O2 ---- O4 ---- O3
```

Some keys previously handled by O3 may now be handled by O4.

Most other mappings remain unchanged.

---

## 11.3 Why Consistent Hashing is useful

The major advantage is:

> **Minimal remapping when instances are added or removed.**

This is very useful for distributed caches.

Example:

```text
Application
     |
     v
Consistent Hashing
     |
 +---+---+---+
 |   |   |   |
 v   v   v   v
O1  O2  O3  O4
```

Suppose O2 disappears.

We don't want to move every cache key.

Instead:

```text
O2's keys → reassigned
Other keys → mostly unchanged
```

---

## 11.4 Virtual Nodes

A physical instance can have multiple positions on the hash ring.

Instead of:

```text
O1 → 1 position
O2 → 1 position
O3 → 1 position
```

we use:

```text
O1 → O1-1, O1-2, O1-3
O2 → O2-1, O2-2, O2-3
O3 → O3-1, O3-2, O3-3
```

These are called **virtual nodes**.

### Why?

They improve distribution.

```text
Physical O1
   |
   +-- Virtual Node 1
   +-- Virtual Node 2
   +-- Virtual Node 3
```

If O2 goes down:

```text
O2
 |
 +-- Virtual Node 1 → removed
 +-- Virtual Node 2 → removed
 +-- Virtual Node 3 → removed
```

The keys around those virtual nodes are redistributed.

---

## 11.5 Consistent Hashing vs IP Hash

These are easy to confuse.

### IP Hash

```text
Client IP
    |
    v
Hash
    |
    v
Backend Instance
```

Example:

```text
192.168.1.10 → O1
192.168.1.11 → O3
```

### Consistent Hashing

```text
Key
 |
 v
Hash
 |
 v
Hash Ring
 |
 v
Instance
```

The key could be:

```text
User ID
Order ID
Cache Key
Session ID
```

So:

> **IP Hash is specifically based on client IP, while Consistent Hashing is a general hashing technique that maps keys to nodes while minimizing remapping when nodes change.**

---

# 12. Important Comparison

| Algorithm                  | Main Decision Factor   | Same Client → Same Instance? | Considers Current Load? |
|----------------------------|------------------------|-----------------------------:|------------------------:|
| Round Robin                | Sequence               |                           No |                      No |
| Weighted Round Robin       | Configured weight      |                           No |                      No |
| Least Connections          | Active connections     |                           No |                     Yes |
| Weighted Least Connections | Connections + capacity |                           No |                     Yes |
| Random                     | Random choice          |                           No |                      No |
| IP Hash                    | Client IP              |                      Usually |                      No |
| Sticky Session             | Session affinity       |                          Yes |                      No |
| Resource-Based             | CPU/memory/etc.        |                           No |                     Yes |
| Consistent Hashing         | Key + hash ring        |                    Key-based |          Not inherently |

---
