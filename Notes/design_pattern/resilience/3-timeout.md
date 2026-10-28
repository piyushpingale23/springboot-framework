# Timeout Pattern in Microservices

## 1. What Is the Timeout Pattern?

The **Timeout Pattern** limits how long a service waits for a response from another service or operation.

If the operation does not complete within the configured duration, the caller stops waiting and handles the timeout.

## 2. Why Do We Need Timeout?

Suppose the Order Service calls a slow Payment Service.

```text
Order Service
     |
     v
Payment Service
     |
     v
Request takes too long
     |
     v
Timeout occurs
     |
     v
Handle the timeout
```

Without an effective timeout, requests may wait too long, consuming threads and other resources.

## 3. How Does Timeout Work?

Suppose the configured timeout is 2 seconds.

```text
Request starts at T = 0

T = 1 second -> Still waiting
T = 2 seconds -> Timeout deadline reached

Caller handles the timeout.
```

If the response arrives within 2 seconds, the caller receives it normally.

If it does not, timeout handling takes place.

**Important:** A client-side timeout does not necessarily cancel the downstream operation. The remote service may continue processing the request.

## 4. Types of Timeouts

| Type                  | Purpose                                                   |
|-----------------------|-----------------------------------------------------------|
| Connection Timeout    | Limits how long it takes to establish a connection        |
| Response/Read Timeout | Limits waiting for response data, depending on the client |
| Overall Deadline      | Limits the total time allowed for an operation            |

Exact timeout semantics vary by HTTP client. A response/read timeout does not always represent a strict end-to-end deadline.

## 5. Timeout vs Retry vs Circuit Breaker

| Pattern         | Purpose                                          |
|-----------------|--------------------------------------------------|
| Timeout         | Limits waiting duration                          |
| Retry           | Attempts an operation again                      |
| Circuit Breaker | Blocks calls when a dependency appears unhealthy |

These patterns complement one another.

## 6. Spring Boot Example

Using Spring's `RestClient` with the Apache HttpComponents 5 request factory:

```java
@Bean
RestClient paymentRestClient() {

    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(Timeout.ofSeconds(1))
        .setResponseTimeout(Timeout.ofSeconds(2))
        .build();

    CloseableHttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .build();

    HttpComponentsClientHttpRequestFactory factory =
        new HttpComponentsClientHttpRequestFactory(httpClient);

    return RestClient.builder()
        .requestFactory(factory)
        .baseUrl("http://payment-service")
        .build();
}
```

Required imports and compatible dependencies must be configured for Spring Framework and Apache HttpComponents versions used by the project.

Here:

- Connection-pool acquisition is limited to 1 second.
- Response waiting is configured for 2 seconds.
- The exact behavior depends on the HTTP client and its timeout semantics.

For strict end-to-end deadlines, configure and propagate an appropriate overall deadline as well.

## 7. Important Considerations

- Choose timeouts based on service latency objectives.
- Set connection and response timeouts explicitly.
- Consider the total request deadline across multiple downstream calls.
- A timeout does not prove that the remote operation failed.
- Combine timeouts with circuit breakers and bounded retries where appropriate.

## 8. Summary

The Timeout Pattern prevents callers from waiting indefinitely for slow operations.

**Remember:** Timeout means *stop waiting after the allowed duration*.
