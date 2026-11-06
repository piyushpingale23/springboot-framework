
# CAP Theorem — Distributed Systems

## 1. What is CAP Theorem?

CAP stands for:

1. **C – Consistency**
2. **A – Availability**
3. **P – Partition Tolerance**

The CAP Theorem states:

> In a distributed data system, when a network partition occurs, the system must choose between Consistency and Availability.

It means a distributed system cannot guarantee both strong consistency and availability during a network partition.

**Important:** CAP does not mean that a system can only support two out of three properties under all conditions. The trade-off becomes unavoidable specifically during a network partition.

---

## 2. Why Do We Need CAP Theorem?

A distributed system consists of multiple computers (nodes) communicating over a network and working together.

Example:

```text
       Distributed Database

       Node A       Node B       Node C
          |            |            |
          +------------+------------+
                  Network
```

Suppose all three nodes maintain copies of customer data.

Initially:

```text
Node A = ₹1,000
Node B = ₹1,000
Node C = ₹1,000
```

A customer deposits ₹500 into their account through Node A.

```text
Step 1: Customer deposits ₹500

Node A = ₹1,500
Node B = ₹1,000
Node C = ₹1,000


Step 2: Replication completes

Node A = ₹1,500
Node B = ₹1,500
Node C = ₹1,500
```

Now imagine Node A cannot communicate with Node B and Node C because of a network failure.

What should happen?

- Should Node A accept requests?
- Should Node B and Node C return their locally available data?
- Should requests be rejected until consistency can be guaranteed?

These are the problems explained by CAP Theorem.

---

## 3. Three Properties of CAP Theorem

### 3.1 Consistency (C)

Consistency means that every successful read returns the latest successfully committed write, according to the system's consistency guarantee.

Example:

Initially:

```text
Node A = ₹1,000
Node B = ₹1,000
```

Customer deposits ₹500.

```text
After Update:

Node A = ₹1,500
Node B = ₹1,000
```

Replication has not completed yet.

If a user reads data from Node B and receives ₹1,000 even though ₹1,500 is the latest committed value, this is a stale read.

It violates strong consistency.

After replication:

```text
Node A = ₹1,500
Node B = ₹1,500
```

Both nodes now have the latest value.

**Important points:**

- Reads should reflect the latest committed write.
- Prevents stale reads under the chosen strong consistency guarantee.
- Important for financial transactions, banking and inventory management.

### 3.2 Availability (A)

Availability means every request received by a non-failing node gets a response within a reasonable time.

The response may contain stale data, depending on the consistency model.

Example:

```text
       Node A             Node B
       -------            -------
       ₹1,500             ₹1,000
          |                  |
          +-------- X -------+
              Network failure
```

A user sends a request to Node B.

Node B cannot communicate with Node A.

There are two possible approaches:

**Option 1: Prioritize Availability**

Node B responds immediately with its locally available value.

```text
Customer
   |
   v
 Node B
   |
   v
Return local data
   |
   v
 ₹1,000
```

The request succeeds, but the value may be stale.

**Option 2: Prioritize Consistency**

Node B cannot verify whether ₹1,000 is the latest value.

Therefore, it rejects or delays the request.

```text
Customer
   |
   v
 Node B
   |
   v
Verify latest data
   |
   v
Cannot communicate with Node A
   |
   v
Reject / Delay request
```

**Important points:**

- Prioritizes successful responses.
- A response may contain stale data.
- Useful for systems where continuous access is important.

### 3.3 Partition Tolerance (P)

Partition Tolerance means the system is designed to handle network communication failures between distributed nodes.

A network partition occurs when nodes cannot communicate with each other because of a network failure.

Normal condition:

```text
       Node A       Node B       Node C
          |            |            |
          +------------+------------+
                Communication
                  Working
```

Network partition:

```text
       Node A              Node B
                              |
                              |
                            Node C

       Node A  X  Node B / Node C

          Network Partition
```

The servers may still be running, but they cannot communicate.

**Important points:**

- Nodes may become isolated from one another.
- Network failures cannot always be prevented.
- Distributed systems must be designed to handle partitions.
- Partition tolerance is a practical requirement for distributed databases.

---

## 4. CAP Trade-off

During a network partition, the system has two major choices:

1. CP – Consistency + Partition Tolerance
2. AP – Availability + Partition Tolerance

### 4.1 CP System (Consistency + Partition Tolerance)

A CP system prioritizes consistency over availability during a network partition.

Example:

```text
       Node A             Node B
       ₹1,500             ₹1,000
          |                  |
          +-------- X -------+
              Partition
```

A customer requests their balance from Node B.

Node B cannot communicate with Node A.

It cannot verify whether ₹1,000 is the latest value.

Therefore, it may reject or delay the request.

```text
Customer
   |
   v
 Node B
   |
   v
Can I verify latest data?
   |
   v
  NO
   |
   v
Reject / Wait for request
```

**Characteristics:**

- Consistency is prioritized.
- Availability may be sacrificed for affected requests.
- Partition tolerance is supported.
- Avoids returning unverified stale data where strong consistency is required.

**Use cases:**

- Banking transactions
- Financial ledgers
- Inventory reservation
- Systems where conflicting updates can cause serious problems

**Examples:**

- Apache ZooKeeper
- etcd

These systems are commonly used for coordination and strongly consistent metadata operations.

### 4.2 AP System (Availability + Partition Tolerance)

An AP system prioritizes availability over strong consistency during a network partition.

Example:

```text
       Node A             Node B
       ₹1,500             ₹1,000
          |                  |
          +-------- X -------+
              Partition
```

A customer requests their balance from Node B.

Node B cannot communicate with Node A.

Instead of rejecting the request, Node B responds using its locally available data.

```text
Customer
   |
   v
 Node B
   |
   v
Can I communicate with Node A?
   |
   v
  NO
   |
   v
Return local available data
   |
   v
 ₹1,000
```

When the network connection is restored, nodes can exchange updates and reconcile their data according to conflict-resolution rules.

This behavior is commonly associated with eventual consistency.

**Characteristics:**

- Availability is prioritized.
- Strong consistency may be relaxed during a partition.
- Partition tolerance is supported.
- Replicas may temporarily contain different values.

**Use cases:**

- Social media likes and comments
- Product view counts
- Shopping cart data
- Distributed content systems

**Examples:**

- Apache Cassandra
- Amazon DynamoDB (depending on configuration and consistency requirements)

---

## 5. CP vs AP Comparison

| Property            | CP                                          | AP                           |
|---------------------|---------------------------------------------|------------------------------|
| Consistency         | Prioritized                                 | Relaxed during partition     |
| Availability        | May be sacrificed                           | Prioritized                  |
| Partition Tolerance | Yes                                         | Yes                          |
| Stale Reads         | Avoided when strong consistency is required | May be allowed               |
| Request Behavior    | Reject or delay some requests               | Respond using available data |
| Typical Use Cases   | Banking, inventory                          | Social media, shopping carts |

---

## 6. What About CA?

CA stands for:

- C – Consistency
- A – Availability

A CA system provides consistency and availability when there is no network partition.

Example:

```text
       Application
           |
           v
      Single Database
```

A single database server does not have internode communication partitions because it has only one database node.

However, if we introduce multiple distributed nodes, we must account for network partitions.

During a network partition, a distributed system cannot guarantee both strong consistency and availability.

**Important interview point:**

CAP does not mean every system permanently chooses only two properties. The trade-off between consistency and availability becomes unavoidable specifically during a network partition.

---

## 6.1 Understanding Application Instances, Databases and Network Partitions

It is important to distinguish between application instances, database nodes and network partitions.

### A. Are three application instances the same as three network partitions?

No. Application instances are running copies of an application. They are not network partitions.

For example:

```text
                 Load Balancer
                /      |      \
             App-1   App-2   App-3
                \      |      /
                 Shared Database
```

Here, App-1, App-2 and App-3 are three application instances.

This is a distributed application architecture, but a network partition has not necessarily occurred.

A **network partition** occurs when nodes that need to communicate with one another become unable to communicate because of a network failure.

Also, having multiple application instances alone does not automatically create the CAP trade-off. CAP is about distributed data systems and their consistency and availability when communication between relevant nodes is interrupted.

### B. What if we have two application instances and two separate databases?

Consider this architecture:

```text
       App-1                 App-2
         |                     |
        DB-1                  DB-2
```

Initially, both databases contain the same data:

| Database | User balance |
|----------|-------------:|
| DB-1     |      ₹10,000 |
| DB-2     |      ₹10,000 |

Now App-1 updates DB-1, but the update has not reached DB-2:

| Database | User balance |
|----------|-------------:|
| DB-1     |       ₹8,000 |
| DB-2     |      ₹10,000 |

The databases now contain different values.

This is **data inconsistency**.

However, different data does not by itself prove that a network partition occurred.

The difference might be caused by:

- Delayed replication.
- A failed synchronization process.
- A network partition.

### C. What happens when a network partition occurs between database nodes?

Suppose DB-1 and DB-2 normally replicate data, but a network failure prevents them from communicating:

```text
       DB-1          X          DB-2
      ₹8,000                    ₹10,000

                 Network failure
```

A request arrives at DB-2.

It cannot contact DB-1 to verify whether its local value is the latest value.

The system has two broad choices:

**Option 1: CP — Prioritize Consistency**

- DB-2 rejects or delays operations that cannot safely proceed.
- The system avoids returning an unverified stale value when strong consistency is required.
- Some requests become unavailable during the partition.

**Option 2: AP — Prioritize Availability**

- DB-2 continues responding using locally available data.
- It may return stale data or accept an update based on outdated data.
- The replicas may temporarily contain different values.

When communication is restored, replicas can exchange updates and reconcile differences according to the system's replication and conflict-resolution strategy.

```text
During partition:

       DB-1          X          DB-2
      ₹8,000                    ₹10,000


After communication is restored and reconciliation succeeds:

       DB-1   <------------>   DB-2
      ₹8,000                    ₹8,000
```

**Important:** Partition Tolerance does not mean inconsistency must always occur.

It means the system is designed to keep operating in the presence of network partitions.

Whether temporary inconsistency occurs depends on the consistency and availability choices made by the system.

### D. If we have only one application instance and one database, is the data always consistent?

No.

A single application instance and a single database avoid synchronization between multiple database replicas, but they do not guarantee that every operation preserves correct data.

For example, consider a bank transfer:

- Account A = ₹10,000
- Account B = ₹5,000
- Transfer amount = ₹2,000

The application performs these operations:

1. Deduct ₹2,000 from Account A.
2. Application crashes.
3. Add ₹2,000 to Account B never happens.

The resulting balances are:

| Account | Balance |
|---------|--------:|
| A       |  ₹8,000 |
| B       |  ₹5,000 |

The transfer is incomplete and violates the intended business operation, even though there is only one database.

**Solution:** Use a database transaction so both balance updates commit together or both roll back.

This is an ACID transaction concern, not the CAP consistency-versus-availability trade-off between distributed replicas.

### E. Data Consistency vs CAP Consistency

These terms are related, but they are not interchangeable.

| Term                         | Meaning                                                                                                    |
|------------------------------|------------------------------------------------------------------------------------------------------------|
| Data consistency / integrity | Data satisfies the application's rules and constraints.                                                    |
| CAP consistency              | In the strong (linearized) sense, reads reflect the latest successfully committed write.                   |
| Eventual consistency         | Replicas may temporarily differ but are expected to converge when updates stop and communication recovers. |

**Remember these distinctions:**

- Multiple application instances are not the same thing as network partitions.
- Two databases containing different values means they are inconsistent, but it does not necessarily mean a network partition occurred.
- A network partition means some distributed nodes cannot communicate.
- A single database can still have data-integrity problems, such as an incomplete transfer.
- CAP specifically addresses the trade-off between strong consistency and availability during a network partition.

---

## 7. Complete CAP Visualization

```text
                  CAP Theorem
                      |
          +-----------+-----------+
          |           |           |
          C           A           P
          |           |           |
     Consistency  Availability  Partition
                                 Tolerance


       During Network Partition
                 |
          +-------+-------+
          |               |
          v               v
         CP              AP
          |               |
    Consistency       Availability
    + Partition       + Partition
    Tolerance         Tolerance
```

Remember:

1. Normal network: A distributed system can provide consistency and availability.
2. Network partition + CP: Preserve consistency, potentially reject requests.
3. Network partition + AP: Continue responding, potentially return stale data.

---

## 8. Interview Questions and Answers

**Q1. What is CAP Theorem?**

CAP Theorem states that when a network partition occurs in a distributed system, we must choose between Consistency and Availability while tolerating the partition.

**Q2. What are the three properties of CAP?**

- Consistency
- Availability
- Partition Tolerance

**Q3. What is a network partition?**

A network partition occurs when distributed nodes cannot communicate with each other because of a network failure.

**Q4. What is the difference between CP and AP?**

- CP prioritizes consistency and may reject or delay requests during a network partition.
- AP prioritizes availability and may return stale data during a network partition.

**Q5. Can a distributed system support all three CAP properties?**

A distributed system can provide consistency and availability during normal operation. However, during a network partition, it cannot guarantee both strong consistency and availability.

**Q6. What is CA?**

CA means Consistency and Availability. These can be provided when there is no network partition, but they cannot both be guaranteed during a partition in a distributed system.

**Q7. Which system is suitable for banking applications?**

A consistency-oriented approach is generally preferred for critical financial operations because stale or conflicting data can cause incorrect transactions.

**Q8. Which system is suitable for social media applications?**

An availability-oriented approach can be suitable because temporary inconsistencies, such as slightly outdated like counts, are often acceptable.

**Q9. Is eventual consistency the same as inconsistency?**

No. Eventual consistency allows temporary differences between replicas but expects them to converge when updates stop and communication recovers.

**Q10. Does CAP mean choosing only two properties permanently?**

No. The trade-off between Consistency and Availability applies specifically during a network partition.

---

**One-line revision:**

CAP explains the trade-off between Consistency and Availability when a network Partition occurs in a distributed system.
