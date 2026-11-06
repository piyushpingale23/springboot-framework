# PACELC Theorem — Distributed Systems

## 1. What is PACELC Theorem?

PACELC stands for:

1. **P – Partition**
2. **A – Availability**
3. **C – Consistency**
4. **E – Else**
5. **L – Latency**
6. **C – Consistency**

The PACELC theorem extends the CAP theorem by explaining the trade-offs in a distributed system during both network partitions and normal operation.

**Definition:**

> If there is a Partition (P), a distributed system must trade off between Availability (A) and Consistency (C); Else (E), when the system is operating normally, it must trade off between Latency (L) and Consistency (C).

In simple words:

- During a network partition: Availability vs Consistency.
- During normal operation: Latency vs Consistency.

**Why do we need PACELC?**

CAP explains the trade-off between Availability and Consistency during a network partition.

However, even when there is no network partition, distributed systems still need to make architectural decisions.

For example:

- Should a database wait for replication to complete before responding?
- Should it respond immediately and replicate data in the background?
- Should it prioritize low latency or strong consistency?

PACELC explains these additional trade-offs.

---

## 2. Understanding PACELC Architecture

Imagine an application using a distributed database with three nodes.

```text
             Application
                  |
                  v
          Distributed Database
                  |
        +---------+---------+
        |         |         |
        v         v         v
      Node A   Node B    Node C
```

All three nodes maintain copies of the same data.

Suppose a customer updates their account balance through Node A.

The update must be replicated to Node B and Node C.

There are two possible situations:

1. Network partition occurs.
2. Network partition does not occur (normal operation).

PACELC explains the trade-offs in both situations.

---

## 3. Understanding the PACELC Decision

```text
             Distributed System
                     |
                     v
          Is there a network partition?
                     |
             +-------+-------+
             |               |
            YES              NO
             |               |
             v               v
        P - Partition     E - Else
             |               |
             v               v
           A vs C           L vs C
             |               |
             v               v
      Availability vs   Latency vs
        Consistency     Consistency
```

### During Network Partition (P)

The system must choose between:

- Availability (A)
- Consistency (C)

This is the same trade-off explained by CAP theorem.

### During Normal Operation (E)

The system must choose between:

- Latency (L)
- Consistency (C)

This is the additional trade-off introduced by PACELC.

---

## 4. P → A vs C (During Network Partition)

A network partition occurs when distributed nodes cannot communicate with each other because of a network failure.

Example:

```text
       Node A             Node B
       -------            -------
       ₹1,500             ₹1,000
          |                  |
          +-------- X -------+
              Partition
```

Node A and Node B cannot communicate.

A customer requests their account balance from Node B.

Node B cannot verify whether ₹1,000 is the latest value.

The system has two choices.

### Option 1: Prioritize Availability (A)

Node B responds using its locally available data.

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

**Characteristics:**

- Requests continue to receive responses.
- Stale data may be returned.
- Availability is prioritized.
- Consistency is relaxed during the partition.

This is an AP-style approach.

### Option 2: Prioritize Consistency (C)

Node B cannot verify the latest data.

Therefore, it rejects or delays the request rather than returning an unverified stale value.

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

**Characteristics:**

- Strong consistency is prioritized.
- Some requests may fail or be delayed.
- Availability is sacrificed for affected requests.
- Partition tolerance is supported.

This is a CP-style approach.

**Conclusion:**

During a network partition:

```text
P → A vs C

Availability vs Consistency
```

---

## 5. E → L vs C (During Normal Operation)

This is the most important additional concept in PACELC.

When there is no network partition, all database nodes can communicate normally.

```text
             Node A
                |
                | Replication
                |
             Node B
                |
                | Replication
                |
             Node C
```

Suppose a customer updates their account balance on Node A.

Node A needs to replicate that update to Node B and Node C.

Now the system has two possible approaches.

### Option 1: Prioritize Consistency (C)

Node A waits for the required replica acknowledgements before responding successfully to the client.

```text
Client
  |
  v
Node A
  |
  v
Replicate update
  |
  +-----------> Node B
  |                |
  |<---------------+
  |
  +-----------> Node C
  |                |
  |<---------------+
  |
  v
Return success
```

**Characteristics:**

- Waits for the required replication acknowledgements.
- Helps ensure replicas have received the update before success is returned.
- Reduces the chance of stale reads, depending on the protocol.
- Higher response latency due to network communication.

**Advantages:**

- Stronger consistency guarantees.
- Better protection against stale reads.
- Useful when data correctness is important.

**Disadvantages:**

- Higher write latency.
- More network round trips.
- Slower responses when replicas are geographically distant.

### Option 2: Prioritize Latency (L)

Node A response to the client without waiting for all replicas to acknowledge the update.

Replication happens asynchronously in the background.

```text
Client
  |
  v
Node A
  |
  +-----------------> Client
  |                    Success
  |
  +-----------------> Node B
  |                    Async replication
  |
  +-----------------> Node C
                       Async replication
```

**Characteristics:**

- Responds without waiting for all replicas.
- Replication happens asynchronously.
- Replicas may temporarily contain stale data.
- Lower response latency.

**Advantages:**

- Faster client response.
- Lower network waiting time.
- Useful when low latency is important.

**Disadvantages:**

- Replicas may not immediately contain the latest data.
- Reads from other nodes may return stale values.
- Additional mechanisms may be needed to handle replication failures.

**Conclusion:**

During normal operation:

```text
E → L vs C

Latency vs Consistency
```

**Important:** Waiting for replication acknowledgements does not automatically guarantee strong consistency. The actual guarantee depends on the replication protocol, read/write rules, quorum configuration and failure handling.

---

## 6. CAP vs PACELC Theorem

| CAP Theorem                                                       | PACELC Theorem                                                  |
|-------------------------------------------------------------------|-----------------------------------------------------------------|
| Explains distributed-system trade-offs during network partitions. | Explains trade-offs during both partition and normal operation. |
| Focuses on C, A and P.                                            | Focuses on P, A, C, E, L and C.                                 |
| Partition: Availability vs Consistency.                           | Partition: Availability vs Consistency.                         |
| Does not explicitly cover normal-operation latency trade-offs.    | Covers normal-operation Latency vs Consistency.                 |
| Simpler model.                                                    | More comprehensive model.                                       |

### Complete Visualization

```text
                  CAP
                   |
                   v
          Network Partition
                   |
                   v
                 A vs C
                   |
       Availability vs Consistency


                 PACELC
                   |
          +--------+--------+
          |                 |
          v                 v
      Partition           Else
          |                 |
          v                 v
        A vs C             L vs C
          |                 |
          v                 v
   Availability vs     Latency vs
    Consistency        Consistency
```

---

## 7. Interview Questions and Answers

**Q1. What is PACELC Theorem?**

PACELC states that if there is a Partition (P), a distributed system must trade off between Availability (A) and Consistency (C); Else (E), during normal operation, it must trade off between Latency (L) and Consistency (C).

**Q2. Why do we need PACELC when CAP already exists?**

CAP explains the Availability vs Consistency trade-off during network partitions. PACELC additionally explains the Latency vs Consistency trade-off during normal operation.

**Q3. What does P represent in PACELC?**

P represents Partition, which means a network failure prevents distributed nodes from communicating with each other.

**Q4. What does E represent in PACELC?**

E stands for Else. It represents normal operating conditions when there is no network partition.

**Q5. What is the difference between A and L in PACELC?**

- Availability focuses on whether the system can successfully respond to requests during a network partition.
- Latency focuses on how quickly the system responds during normal operation.

**Q6. Why does consistency increase latency?**

In some distributed systems, maintaining stronger consistency requires waiting for replication acknowledgements or coordination between nodes. This adds network communication time and increases latency.

**Q7. What is PA/EL?**

- PA: Prioritize Availability during a partition.
- EL: Prioritize Low Latency during normal operation.

**Q8. What is PC/EC?**

- PC: Prioritize Consistency during a partition.
- EC: Prioritize Consistency during normal operation.

**Q9. Which theorem is more comprehensive: CAP or PACELC?**

PACELC is more comprehensive because it explains trade-offs during both network partitions and normal operation.

**Q10. Does PACELC mean that low latency always results in weak consistency?**

No. It explains a design trade-off. The actual relationship depends on the replication architecture, consistency protocol, network topology and database configuration.

---

**One-line revision:**

PACELC explains that during a network Partition, a distributed system trades off Availability and Consistency; Else, during normal operation, it trades off Latency and Consistency.
