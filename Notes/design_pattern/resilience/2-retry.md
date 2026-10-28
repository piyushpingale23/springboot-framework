# Retry Pattern in Microservices

## 1. What Is the Retry Pattern?

The **Retry Pattern** is a resilience pattern that automatically repeats a failed operation when the failure may be temporary.

It helps handle transient failures such as temporary network issues or service unavailability.

## 2. Why Do We Need Retry?

Suppose the Order Service calls the Payment Service.

```text
Order Service
     |
     v
Payment Service
     |
     v
Temporary Network Failure
     |
     v
Retry Request
     |
     v
Payment Service
     |
     v
Success
```

Without retries, a temporary failure may cause the entire operation to fail unnecessarily.

## 3. How Does Retry Work?

Example configuration:

```text
Maximum attempts = 3
Initial delay = 1 second
```

```text
Attempt 1 -> Failure
     |
     v
Wait 1 second
     |
     v
Attempt 2 -> Failure
     |
     v
Wait
     |
     v
Attempt 3 -> Success
```

If all attempts fail, return an error or execute appropriate fallback handling.

**Note:** Maximum attempts may include the initial attempt or count retries separately, depending on the library configuration.

## 4. Retry Strategies

| Strategy            | Description                                              |
|---------------------|----------------------------------------------------------|
| Fixed Delay         | Wait the same duration between attempts                  |
| Exponential Backoff | Increase the delay after each failure                    |
| Jitter              | Add randomness to delays to prevent synchronized retries |

Example exponential backoff:

```text
Attempt 1 -> Failure
Wait 1 second

Attempt 2 -> Failure
Wait 2 seconds

Attempt 3 -> Failure
Wait 4 seconds
```

## 5. Retry vs Circuit Breaker

| Retry                           | Circuit Breaker                            |
|---------------------------------|--------------------------------------------|
| Repeats a failed operation      | Stops calls to an unhealthy dependency     |
| Useful for transient failures   | Useful for persistent failures or slowness |
| Can increase downstream traffic | Reduces unnecessary downstream traffic     |

## 6. Spring Boot Using Resilience4j

```java
@Service
public class InventoryService {

    @Retry(name = "inventoryService")
    public String checkInventory() {

        // Call the downstream Inventory Service.
        return inventoryClient.check();
    }
}
```

Example configuration:

```yaml
resilience4j:
  retry:
    instances:
      inventoryService:
        maxAttempts: 3
        waitDuration: 1s
```

The example assumes `inventoryClient` is an injected HTTP client and the compatible Resilience4j Spring Boot integration is configured.

## 7. Important Considerations

- Retry only failures that may be temporary.
- Limit the number of attempts.
- Use backoff and jitter where appropriate.
- Configure timeouts so retries cannot wait indefinitely.
- Avoid retrying non-idempotent operations blindly.
- Avoid excessive retries across multiple service layers.

For payment operations, use idempotency keys or equivalent duplicate-prevention mechanisms. A timeout does not prove that a payment failed.

## 8. Summary

The Retry Pattern improves resilience by repeating operations that fail temporarily.

**Remember:** Retry means *try again*.
