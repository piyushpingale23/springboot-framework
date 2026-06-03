# Stateful vs Stateless Applications

## Overview

The difference between a Stateful and Stateless application is whether the server remembers information about previous requests from a client.

---

# 1. Stateless Application

A Stateless application does not store client-specific session data on the server.

Each request is independent and contains all information required to process it.

## Example: REST API with JWT Authentication

### Request 1

```http
GET /profile
Authorization: Bearer eyJhbGciOi...
```

Server:
- Validates JWT token
- Returns user profile

### Request 2

```http
GET /orders
Authorization: Bearer eyJhbGciOi...
```

Server:
- Again validates JWT token
- Returns orders

The server does not remember Request 1.

Each request is processed independently.

---

## Stateless Flow

```text
Request 1 ---> Server
Request 2 ---> Server
Request 3 ---> Server

No session data stored.
```

---

## Characteristics

- No session stored on server
- Every request is independent
- Easy to scale horizontally
- Better suited for cloud-native applications
- Common in REST APIs and Microservices

---

## Real Examples

- REST APIs
- JWT Authentication
- Spring Boot Microservices
- Public APIs

---

# 2. Stateful Application

A Stateful application stores client session data on the server.

The server remembers information between requests.

---

## Example: Session-Based Login

### Login Request

```http
POST /login
username=admin
password=admin123
```

Server:

```text
Session ID = ABC123
User = admin
```

Session is stored in server memory.

---

### Profile Request

```http
GET /profile
Cookie: JSESSIONID=ABC123
```

Server:

```text
Find Session ABC123
User = admin
```

Returns profile data.

The server remembers the previous login request.

---

## Stateful Flow

```text
Client
   |
   v
Server
   |
   +--> Session Data
          |
          +--> User = admin
          +--> Session = ABC123
```

---

## Characteristics

- Session stored on server
- Server remembers client information
- More difficult to scale
- Common in traditional web applications

---

## Real Examples

- Servlet/JSP Applications
- Session-based Authentication
- Shopping Cart stored in HTTP Session
- Banking Portals using Server Sessions

---

# Load Balancing

A Load Balancer distributes incoming requests across multiple servers.

---

# Round Robin Load Balancing

Requests are distributed sequentially among available servers.

## Example

```text
Request 1 ---> Server A
Request 2 ---> Server B
Request 3 ---> Server C
Request 4 ---> Server A
Request 5 ---> Server B
```

### Visualization

```text
             +--> Server A
Client ----->+--> Server B
             +--> Server C
```

### Best For

- Stateless Applications
- REST APIs
- Microservices

### Why?

Because any server can handle any request.

No session information exists on a specific server.

---

# Sticky Session Load Balancing

Also called:

- Session Affinity
- Session Persistence

The load balancer always sends requests from the same user to the same server.

---

## Example

### Login Request

```text
User Login ---> Server A
```

Server A creates:

```text
Session ID = ABC123
```

### Future Requests

```text
Profile ---> Server A
Orders  ---> Server A
Logout  ---> Server A
```

All requests go to the same server.

---

## Visualization

```text
              +--> Server A (Session ABC123)
Client -------|
              |
              +--> Server B
              +--> Server C
```

Load balancer remembers:

```text
User X --> Server A
```

---

## Why Sticky Sessions?

Suppose:

```text
Server A stores Session ABC123
```

Request Flow:

```text
Login ---> Server A
Profile ---> Server B
```

Server B does not know Session ABC123.

Result:

```text
User appears logged out.
```

Sticky Sessions solve this problem.

---

# Alternative to Sticky Sessions

Store sessions in a shared storage.

Example:

- Redis
- Database
- Distributed Cache

## Visualization

```text
              +--------+
Server A ---->| Redis  |
              +--------+

Server B ---->| Redis  |

Server C ---->| Redis  |
```

Now any server can retrieve session data.

No Sticky Session required.

---

# Stateful vs Stateless Comparison

| Feature                   | Stateful       | Stateless       |
|---------------------------|----------------|-----------------|
| Session Storage           | Server         | Client (JWT)    |
| Server Remembers Requests | Yes            | No              |
| Easy Horizontal Scaling   | No             | Yes             |
| Requires Sticky Sessions  | Usually Yes    | No              |
| Load Balancer Type        | Sticky Session | Round Robin     |
| Performance               | Lower at Scale | Better at Scale |
| Microservices Friendly    | No             | Yes             |
| Common Authentication     | JSESSIONID     | JWT             |

---

## Why do Stateless Applications Scale Better?

Because:

```text
Request 1 ---> Server A
Request 2 ---> Server B
Request 3 ---> Server C
```

No session data exists on any specific server.

Any server can process any request.

This makes horizontal scaling very easy.

---

## Stateful

```text
Server Remembers
Client Sends Session ID
```

Example:

```text
JSESSIONID Authentication
```

Uses:

```text
Sticky Session Load Balancing
```

---

# Final Visualization

```text
STATELESS

Client
   |
   +--> Server A
   +--> Server B
   +--> Server C

No session stored.

Load Balancer:
Round Robin


STATEFUL

Client
   |
   +--> Server A
           |
           +--> Session Data

Future requests must go to Server A.

Load Balancer:
Sticky Session
```