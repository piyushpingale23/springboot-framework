# Rate Limiting Pattern

## 1. Definition

The Rate Limiting Pattern restricts the number of requests a client can make within a specified period.

It protects applications and APIs from excessive traffic, abuse, and resource exhaustion.

**Example:** Allow a user to make a maximum of 100 API requests per minute.

## 2. Request Throttling

Request throttling controls or slows down incoming requests when traffic exceeds a defined limit.

```text
Client
   |
   v
Rate Limiter
   |
   +---- Within limit ----> Process Request
   |
   +---- Limit exceeded --> Reject Request
```

## 3. Fixed Window Algorithm

Divides time into fixed intervals and counts requests within each interval.

**Example:** Allow 100 requests per minute.

```text
10:00:00 - 10:00:59 -> Maximum 100 requests
10:01:00 - 10:01:59 -> Counter resets
```

**Limitation:** A client can send 100 requests at the end of one window and another 100 at the beginning of the next window.

## 4. Sliding Window Algorithm

Tracks requests over a continuously moving time window, reducing sudden bursts around fixed-window boundaries.

**Example:** At 10:00:30, count requests made during the previous 60 seconds.

```text
Current time: 10:00:30

Count requests from:
09:59:30 ------------> 10:00:30
       Sliding 60-second window
```

**Benefit:** Provides smoother and more accurate rate limiting than a basic fixed window.

## 5. Token Bucket Algorithm

Tokens are added to a bucket at a fixed rate. Each request consumes one or more tokens.

If tokens are available, the request is accepted. Otherwise, it is rejected or delayed.

**Example:** Bucket capacity = 10 tokens; refill rate = 2 tokens/second.

- A burst of up to 10 requests can be accepted when the bucket is full.
- Tokens refill at 2 per second.
- Requests without available tokens are rejected or delayed, depending on implementation.

**Benefit:** Allows controlled bursts while limiting the long-term request rate.

## 6. Leaky Bucket Algorithm

Requests enter a bucket and are processed at a controlled rate, like water leaking from a container.

```text
Incoming Requests
        |
        v
   +-----------+
   |   Queue   |
   +-----------+
        |
        v
  Fixed Processing Rate
```

If the bucket or queue is full, additional requests are rejected.

**Benefit:** Smooths bursts and produces a more consistent output rate.

## 7. HTTP 429 — Too Many Requests

HTTP status code `429` indicates that a client has exceeded the permitted request rate.

Example response:

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 30
Content-Type: application/json
```

```json
{
  "error": "Rate limit exceeded",
  "message": "Please try again later."
}
```

`Retry-After` can tell the client how long to wait before retrying.

## 8. Where Is Rate Limiting Used?

- API Gateways
- Login and authentication endpoints
- Payment APIs
- Public REST APIs
- Preventing abusive traffic and excessive requests

Examples of technologies that support rate limiting include NGINX, API gateways, Redis-based implementations, and cloud API management services.

## 9. Quick Comparison

| Algorithm      | Main Idea                                            |
|----------------|------------------------------------------------------|
| Fixed Window   | Count requests in fixed time intervals               |
| Sliding Window | Count requests in a moving time interval             |
| Token Bucket   | Tokens allow requests and controlled bursts          |
| Leaky Bucket   | Queue requests and process them at a controlled rate |

## 10. Key Point

**Rate Limiting controls how many requests a client can make within a given time, helping protect APIs from abuse and overload.**