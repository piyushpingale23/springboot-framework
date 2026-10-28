# Saga Pattern

## 1. What is Saga?

**Saga is a distributed transaction management / application design pattern used when one business operation spans multiple independent microservices.**

A Saga breaks one large business transaction into multiple **local transactions**, where each service manages its own transaction.

If a later step fails, Saga uses **compensating transactions** to logically undo the business effects of previously completed steps.

```text
One Business Operation
        |
        +-------------------+
        |                   |
        v                   v
   Service A            Service B
      |                     |
   Local Tx              Local Tx
      |                     |
   DB A                  DB B
```

The key idea:

```text
Saga = Multiple Local Transactions + Business-level Coordination + Compensating Transactions
```

---

# 2. Why do we need Saga?

In a monolithic application, multiple operations can often participate in one database transaction.

```text
Application
     |
     +----------------------+
     |          |           |
     v          v           v
  Order      Payment    Inventory
     |          |           |
     +----------+-----------+
                |
             One DB
```

We can do:

```java
@Transactional
public void placeOrder() {

    createOrder();

    makePayment();

    reserveInventory();
}
```

If something fails:

```text
Create Order       SUCCESS
       |
Make Payment       SUCCESS
       |
Reserve Inventory  FAILED
       |
       v
    ROLLBACK
       |
       +--> Order undone
       +--> Payment undone
```

The database transaction provides atomicity.

---

# 3. The problem in Microservices

Now suppose we split the application into microservices.

```text
Order Service
      |
      v
   Order DB

Payment Service
      |
      v
  Payment DB

Inventory Service
      |
      v
 Inventory DB
```

One business operation may involve all three services:

```text
Place Order
    |
    +--> Order Service
    |
    +--> Payment Service
    |
    +--> Inventory Service
```

Each service has its own local transaction.

```text
Order DB       Payment DB       Inventory DB
   |              |                  |
Local Tx       Local Tx           Local Tx
   |              |                  |
 COMMIT          COMMIT             FAIL
```

A normal:

```
@Transactional
```

cannot simply provide one atomic transaction across all these independent databases.

We therefore need a different approach.

This is where Saga can help.

---

# 4. Saga is NOT one big transaction

This is the most important concept.

Saga does NOT create:

```text
BEGIN
    Order
    Payment
    Inventory
COMMIT
```

across all services.

Instead:

```text
Order Service
    |
    | Local Transaction
    v
   COMMIT
    |
    v
Payment Service
    |
    | Local Transaction
    v
   COMMIT
    |
    v
Inventory Service
    |
    | Local Transaction
    v
   FAILURE
```

Each service has already committed its own local transaction.

---

# 5. Example: Order Processing

Suppose placing an order requires:

```text
1. Create Order
2. Make Payment
3. Reserve Inventory
```

Saga flow:

```text
Create Order
     |
     v
   SUCCESS
     |
     v
Make Payment
     |
     v
   SUCCESS
     |
     v
Reserve Inventory
     |
     v
   FAILURE
```

At this point:

```text
Order DB
Order = CREATED

Payment DB
Payment = SUCCESS

Inventory DB
Reservation = FAILED
```

The Order and Payment transactions are already committed.

We cannot simply execute:

```text
ROLLBACK EVERYTHING
```

---

# 6. Compensating Transaction

Instead, Saga performs **compensating actions**.

For example:

```text
Original operation:

Create Order
     |
     v
Make Payment
     |
     v
Reserve Inventory
     |
     X FAILURE
```

Compensation:

```text
Refund Payment
     |
     v
Cancel Order
```

So the final state becomes:

```text
Order DB
Order = CANCELLED

Payment DB
Payment = REFUNDED

Inventory DB
Reservation = NOT RESERVED
```

Important:

**The original transactions were not rolled back.**

Instead:

```text
Payment SUCCESS
       |
       v
Payment REFUNDED
```

and:

```text
Order CREATED
       |
       v
Order CANCELLED
```

These are **new transactions**.

---

# 7. What is a Compensating Transaction?

A compensating transaction is a new business operation that logically reverses the effect of a previous successful operation.

Examples:

```text
Original                    Compensation

Create Order       --->     Cancel Order

Payment            --->     Refund Payment

Reserve Inventory  --->     Release Inventory

Create Booking     --->     Cancel Booking
```

It is important to understand:

```text
Compensation != Database Rollback
```

A database rollback happens inside the original transaction.

A compensation happens later as a **new transaction**.

---

# 8. Saga and ACID

`@Transactional` and Saga solve problems at different levels.

## Local transaction

```text
@Transactional
      |
      v
Local Database
      |
      v
ACID
```

It provides atomicity within that transaction.

## Saga

```text
Saga
  |
  +--> Service A → Local Transaction
  |
  +--> Service B → Local Transaction
  |
  +--> Service C → Local Transaction
```

Saga coordinates these independent transactions at the **business/application level**.

Therefore:

```text
@Transactional
    ↓
Local Transaction

Saga
    ↓
Distributed Business Transaction
```

---

# 9. Is Saga a Database concept?

No.

Saga is primarily an **application/distributed-system design pattern**.

However, Saga exists because distributed applications commonly have:

```text
Multiple Services
       |
       v
Independent Data Stores
       |
       v
Independent Local Transactions
```

Therefore, Saga deals heavily with transaction boundaries and data consistency, but Saga itself is not a database feature.

---

# 10. Is Saga mandatory?

No.

Saga is a **design pattern**, not a compulsory requirement.

Having microservices does NOT automatically mean you must use Saga.

Use Saga when the business operation:

```text
One Business Operation
        |
        v
Multiple Independent Services
        |
        v
Multiple Local Transactions
        |
        v
Failure can leave partial business state
        |
        v
Need Compensation
        |
        v
Saga can be useful
```

If there is no need for compensation or distributed transaction coordination, another design may be sufficient.

---

# 11. Multiple Modules vs Saga

Having multiple modules inside one microservice does NOT automatically mean Saga.

For example:

```text
Recon Service
|
+-- recon-services
+-- recon-feeders
+-- recon-processing-engine
+-- report-engine
|
+-- Recon DB
```

If all modules use the same database and participate in the same local transaction:

```java
@Transactional
public void process() {
    moduleA();
    moduleB();
    moduleC();
}
```

then a normal database transaction may be enough.

```text
Module A
   |
Module B
   |
Module C
   |
   v
Same DB
   |
   v
@Transactional
```

The important question is not:

> "How many modules do I have?"

The important question is:

> "Does one business operation cross independent transaction boundaries that cannot be handled by one local transaction?"

---

# 12. Two Types of Saga

There are two common approaches:

```text
                    Saga
                     |
            +--------+--------+
            |                 |
            v                 v
      Orchestration      Choreography
```

---

# 13. Saga Orchestration

In orchestration, there is a central component called a:

**Saga Orchestrator**

The orchestrator controls the workflow.

```text
                 Saga Orchestrator
                  /       |       \
                 /        |        \
                v         v         v
             Order     Payment   Inventory
             Service    Service    Service
```

The orchestrator tells services what to do.

Example:

```text
Orchestrator
      |
      | Create Order
      v
Order Service
      |
      | SUCCESS
      v
Orchestrator
      |
      | Process Payment
      v
Payment Service
      |
      | SUCCESS
      v
Orchestrator
      |
      | Reserve Inventory
      v
Inventory Service
      |
      | FAILURE
      v
Orchestrator
```

The orchestrator now knows that Inventory failed.

:

```text
Inventory failed
       |
       v
Orchestrator
       |
       +--> Refund Payment
       |
       +--> Cancel Order
```

---

# 14. Does Orchestration need failure events?

Not necessarily.

The important point is:

**The orchestrator owns the Saga workflow.**

Communication can happen through:

```text
REST
gRPC
Messaging
Events
```

For example:

```text
Orchestrator
      |
      | REST
      v
Inventory Service
      |
      | HTTP 500 / failure response
      v
Orchestrator
      |
      v
Start compensation
```

So the service does not need to independently publish a `InventoryFailed` event for other services to discover the failure.

The orchestrator receives the result and decides what happens next.

---

# 15. Saga Choreography

In choreography, there is **no central orchestrator** controlling the workflow.

Services communicate using events.

For example:

```text
Order Service
      |
      | OrderCreated
      v
    Kafka
      |
      v
Payment Service
      |
      | PaymentCompleted
      v
    Kafka
      |
      v
Inventory Service
```

Each service listens to events and decides what it should do.

---

# 16. Choreography Failure Handling

Suppose:

```text
OrderCreated
      |
      v
PaymentCompleted
      |
      v
InventoryReservationFailed
```

The failure is communicated as an event:

```text
Inventory Service
      |
      | InventoryReservationFailed
      v
    Kafka
```

Payment Service can listen for that event:

```text
InventoryReservationFailed
          |
          v
Payment Service
          |
          v
Refund Payment
          |
          v
PaymentRefunded
```

Then Order Service can listen for:

```text
PaymentRefunded
      |
      v
Cancel Order
```

Complete choreography:

```text
Order
  |
  | OrderCreated
  v
Kafka
  |
  v
Payment
  |
  | PaymentCompleted
  v
Kafka
  |
  v
Inventory
  |
  | InventoryReservationFailed
  v
Kafka
  |
  v
Payment
  |
  | Refund
  |
  | PaymentRefunded
  v
Kafka
  |
  v
Order
  |
  | Cancel Order
```

There is no central component saying:

```text
"Now do this."
"Now do that."
```

The services react to events.

---

# 17. Orchestration vs Choreography

## Orchestration

```text
                 Orchestrator
                /      |      \
                               ↓              ↓              ↓
            Order   Payment  Inventory
```

The orchestrator knows:

```text
1. Create Order
2. Make Payment
3. Reserve Inventory
4. If Inventory fails → Refund Payment
5. Cancel Order
```

## Choreography

```text
Order
  |
  | Event
  v
Kafka
  |
  v
Payment
  |
  | Event
  v
Kafka
  |
  v
Inventory
```

Services react to events.

Simple distinction:

```text
Orchestration
    ↓
Central coordinator controls workflow

Choreography
    ↓
Services coordinate through events
```

---

# 18. Failure Flow Comparison

## Orchestration

```text
Inventory
    |
    | FAILED
    v
Orchestrator
    |
    +----> Refund Payment
    |
    +----> Cancel Order
```

## Choreography

```text
Inventory
    |
    | InventoryFailed
    v
  Kafka
    |
    +----> Payment
    |         |
    |         +----> Refund
    |
    +----> Order
              |
              +----> Cancel
```

---

# 19. Saga is about Business Consistency

A very important point:

Saga does not necessarily provide the same immediate atomic consistency as a single database transaction.

Instead, it often provides **eventual consistency**.

Example:

```text
Order = CREATED
Payment = SUCCESS
Inventory = FAILED
```

For a short period, the system may be in this intermediate state.

Then:

```text
Refund Payment
      |
      v
Cancel Order
```

Eventually:

```text
Order = CANCELLED
Payment = REFUNDED
Inventory = NOT RESERVED
```

So:

```text
Normal DB Transaction
      ↓
Atomic consistency

Saga
      ↓
Distributed coordination
      ↓
Compensation
      ↓
Eventual consistency
```

---

# 20. Complete Mental Model

Remember this diagram:

```text
                         SAGA
                           |
             One Business Operation
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
    Order Service    Payment Service   Inventory Service
          |                |                |
      Local Tx          Local Tx          Local Tx
          |                |                |
        COMMIT           COMMIT           FAIL
          |                |                |
          +----------------+----------------+
                           |
                      Compensation
                           |
              +------------+------------+
              |                         |
              v                         v
       Cancel Order              Refund Payment
```

And there are two ways to coordinate it:

```text
                         SAGA
                           |
                 +---------+---------+
                 |                   |
                 v                   v
           Orchestration       Choreography
                 |                   |
                 v                   v
           Orchestrator          Events
                 |                   |
                 v                   v
          Commands/Calls          Kafka
```

---

# 21. Most Important Points to Remember

```text
1. Saga is a distributed transaction management/application design pattern.

2. Saga is mainly useful when one business operation spans multiple
   independent microservices.

3. Each service performs its own local transaction.

4. Local transactions are committed independently.

5. Saga does NOT provide one giant database transaction.

6. If a later step fails, Saga can execute compensating transactions.

7. Compensation is a new transaction, not a database rollback.

8. Orchestration uses a central Saga Orchestrator.

9. Choreography uses events and lets services react to them.

10. Saga is not mandatory just because you use microservices.

11. Multiple modules inside one microservice do not automatically require Saga.

12. Saga is commonly associated with eventual consistency rather than
    one atomic ACID transaction across all services.
```

## One-line definition

> **Saga is a pattern for coordinating a business transaction across multiple independent services by using local transactions and compensating actions when something fails.**
