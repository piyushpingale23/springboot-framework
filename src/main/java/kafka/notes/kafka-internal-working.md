# Apache Kafka — Internal Working Notes

## 1. Core Idea

Kafka is a distributed event streaming platform.

The basic flow is:

```text
Producer
   |
   v
Kafka
   |
   v
Consumer
```

But internally:

```text
Application
    |
    v
Producer
    |
    | Serialize
    v
   Bytes
    |
    v
Kafka Broker
    |
    v
Topic
    |
    v
Partition
    |
    v
Append-only Log
    |
    v
Disk
    |
    v
Consumer
    |
    | Deserialize
    v
Application Object
```

The most important concept:

> Kafka fundamentally stores serialized data as bytes.

Kafka itself does not inherently understand whether the bytes represent a String, Integer, JSON, Java object, etc.

---

# 2. What Does Kafka Store?

Kafka stores records as bytes.

For example:

```text
String
"SUCCESS"
    |
    v
Serializer
    |
    v
bytes
    |
    v
Kafka
```

JSON:

```json
{
  "id": 101,
  "name": "Piyush",
  "amount": 5000
}
```

becomes:

```text
JSON
  |
  v
Serializer
  |
  v
bytes
  |
  v
Kafka
```

Integer:

```text
5000
  |
  v
IntegerSerializer
  |
  v
bytes
  |
  v
Kafka
```

Java object:

```java
Transaction transaction;
```

becomes:

```text
Java Object
     |
     v
Serializer
     |
     v
bytes
     |
     v
Kafka
```

Kafka doesn't need to understand the original type.

---

# 3. Serialization and Deserialization

Producer side:

```text
Java Object / String / Integer / JSON
                |
                v
           Serializer
                |
                v
              bytes
                |
                v
             Kafka
```

Consumer side:

```text
             Kafka
                |
                v
              bytes
                |
                v
          Deserializer
                |
                v
Java Object / String / Integer / JSON
```

So:

```text
Producer:

Object
  ↓
Serializer
  ↓
bytes
  ↓
Kafka


Consumer:

Kafka
  ↓
bytes
  ↓
Deserializer
  ↓
Object
```

---

# 4. Producer Internal Flow

Suppose:

```
producer.send(new ProducerRecord<>("transactions","101","PAYMENT_SUCCESS"));
```

Internally:

```text
Application
     |
     v
ProducerRecord
     |
     +------------------+
     |                  |
    Key                Value
   "101"         "PAYMENT_SUCCESS"
     |                  |
     v                  v
Key Serializer     Value Serializer
     |                  |
     v                  v
   bytes              bytes
          \          /
           \        /
            Producer
               |
               v
          Partitioner
               |
               v
          Kafka Broker
```

---

# 5. Producer Buffer

Before data reaches Kafka, the Producer has internal buffering.

Conceptually:

```text
Spring Boot Application
          |
          v
     Kafka Producer
          |
          v
     Producer Buffer
          |
     +----+----+----+
     |    |    |    |
    TXN1 TXN2 TXN3 TXN4
          |
          v
       Network
          |
          v
     Kafka Broker
```

This is important when Kafka is temporarily unavailable.

---

# 6. Partition Selection

Suppose a topic has 3 partitions:

```text
transactions

Partition 0
Partition 1
Partition 2
```

Producer needs to decide where a record goes.

If a key is supplied:

```text
key = "101"
```

the partitioner can use the key to determine the partition.

Conceptually:

```text
key
 |
 v
hash(key)
 |
 v
partition
```

Example:

```text
"101"
  |
  v
hash
  |
  v
Partition 1
```

Therefore:

```text
Producer
   |
   | key = 101
   v
Partition 1
```

Records with the same key are commonly routed to the same partition, depending on the partitioning configuration.

---

# 7. Topic, Partition and Offset

These three concepts are extremely important.

## Topic

A logical category/name.

Example:

```text
transactions
```

## Partition

A topic is divided into partitions.

```text
transactions
   |
   +-- Partition 0
   +-- Partition 1
   +-- Partition 2
```

## Offset

Every record inside a partition has an offset.

```text
Partition 0

Offset 0 → Record A
Offset 1 → Record B
Offset 2 → Record C
Offset 3 → Record D
```

Offsets are maintained independently per partition.

For example:

```text
Partition 0

0 → A
1 → B
2 → C


Partition 1

0 → X
1 → Y
2 → Z
```

There is no single global offset across all partitions.

---

# 8. Kafka Stores an Append-Only Log

Kafka's fundamental storage model is an append-only log.

Conceptually:

```text
Partition 0

+------+--------+
|Offset| Record |
+------+--------+
|  0   | A      |
|  1   | B      |
|  2   | C      |
|  3   | D      |
+------+--------+
```

New records are appended:

```text
A → B → C → D → E → F
```

Kafka does not normally modify an old record when a consumer reads it.

---

# 9. Kafka Storage on Disk

Kafka ultimately persists its log data on disk.

Conceptually:

```text
Kafka Broker
     |
     +-------------------+
     |                   |
    RAM              Disk
     |                   |
 OS Page Cache       Kafka Log
                         |
                         v
                   Log Segment Files
```

Kafka uses the operating system's page cache and other buffering mechanisms to improve performance.

Therefore, don't think:

```text
Kafka = everything stored permanently in RAM
```

Instead:

```text
Durability
    ↓
Disk

Performance
    ↓
RAM / OS Page Cache / Buffers
```

---

# 10. Kafka Log Segments

Kafka doesn't store an entire partition as one giant file.

Partitions are represented using log segments.

Conceptually:

```text
transactions-0/

00000000000000000000.log
00000000000000000000.index
...

next segment
...
```

As data grows, Kafka rolls to new log segments.

This makes retention and log management practical.

---

# 11. Kafka Record

A Kafka record conceptually contains:

```text
Kafka Record
│
├── Key
├── Value
├── Timestamp
├── Headers
├── Partition
└── Offset
```

Example:

```text
Topic      = transactions
Partition  = 2
Offset     = 10452

Key        = "TXN1001"

Value      = {
               "transactionId": "TXN1001",
               "amount": 5000,
               "status": "SUCCESS"
             }

Headers:
    source = "UPI"
```

The actual internal binary representation contains more details, but this is the useful mental model.

---

# 12. Consumer Internal Flow

Consumer does not directly communicate with the Producer.

The architecture is:

```text
Producer
    |
    v
Kafka
    |
    v
Consumer
```

The Producer and Consumer are decoupled.

Kafka acts as the middle layer.

---

# 13. Consumer Pull Model

Kafka Consumers generally pull records from Kafka.

For example:

```text
Consumer

"Give me records from
 partition 0 starting from offset 100."

              |
              v

Kafka

Partition 0:

98
99
100
101
102
103
...

              |
              v

Consumer receives

100
101
102
...
```

So conceptually:

```text
Consumer
    |
    | FETCH
    v
Kafka
    |
    | records
    v
Consumer
```

---

# 14. Consumer Deserialization

Kafka gives the Consumer bytes.

For example:

```text
Kafka
  |
  v
bytes
  |
  v
Deserializer
  |
  v
Java Object
```

For JSON:

```text
Kafka
  |
  v
JSON bytes
  |
  v
JSON Deserializer
  |
  v
Transaction object
```

---

# 15. Consumer Offset

Suppose:

```text
Partition 0

Offset 0 → A
Offset 1 → B
Offset 2 → C
Offset 3 → D
Offset 4 → E
```

Consumer processes:

```text
A
B
C
```

The Consumer's committed position can indicate that processing has progressed through those records.

Conceptually:

```text
0    1    2    3    4
|    |    |    |    |
A    B    C    D    E
          |
          |
       processed
```

Important:

> Consumer progress does not delete the records from Kafka.

---

# 16. Consumer Does NOT Remove Data

This is a very important difference from a queue mental model.

Suppose:

```text
Kafka

0 → A
1 → B
2 → C
3 → D
```

Consumer reads:

```text
A
B
C
```

Kafka still contains:

```text
0 → A
1 → B
2 → C
3 → D
```

The records are eventually removed according to Kafka's configured retention/cleanup behavior.

---

# 17. Consumer Groups

Suppose:

```text
Topic
 |
 +-- Partition 0
 +-- Partition 1
 +-- Partition 2
```

Consumer group:

```text
orders-group

Consumer A
Consumer B
Consumer C
```

Kafka can distribute partitions:

```text
Partition 0 → Consumer A

Partition 1 → Consumer B

Partition 2 → Consumer C
```

If there are 4 consumers but only 3 partitions:

```text
Partition 0 → Consumer A
Partition 1 → Consumer B
Partition 2 → Consumer C

Consumer D → IDLE
```

Within a consumer group, a partition is assigned to at most one consumer at a time.

Therefore:

```text
Number of active consumers
cannot exceed
number of partitions
```

for a single consumer group.

---

# 18. What Happens If Consumer Is Down?

Suppose:

```text
Kafka

0 → A
1 → B
2 → C
3 → D
4 → E
```

Consumer is down.

Producer can continue producing, assuming Kafka is available and storage/retention constraints are not exceeded.

Kafka continues storing:

```text
0 → A
1 → B
2 → C
3 → D
4 → E
5 → F
6 → G
```

Consumer later comes back:

```text
Consumer
    |
    | start from its offset
    v
Kafka
    |
    v
A B C D E F G
```

Consumer can catch up.

---

# 19. What Happens If Kafka Broker Is Down?

Suppose:

```text
Producer
    |
    v
Kafka Broker
    X
   DOWN
```

The Producer cannot successfully write to that unavailable broker.

However, the Producer has its own internal buffer:

```text
Producer
   |
   v
Producer Buffer

+-------+
| TXN1  |
| TXN2  |
| TXN3  |
+-------+
```

The Producer can retry according to its configuration.

If Kafka becomes available:

```text
Kafka DOWN
    |
    | retry
    v
Kafka UP
    |
    v
Record successfully written
```

If Kafka remains unavailable long enough, the Producer can eventually fail the send after its configured timeout/retry/buffering limits.

Therefore:

> Data sitting only in the Producer's buffer has not yet been durably stored in Kafka.

---

# 20. Important: Topic Does Not Really "Go Down"

A Kafka topic itself isn't normally described as being "down."

A topic consists of partitions:

```text
Topic: transactions

Partition 0
Partition 1
Partition 2
```

Each partition has a leader broker.

For example:

```text
Partition 0 → Broker 1 = Leader
Partition 1 → Broker 2 = Leader
Partition 2 → Broker 3 = Leader
```

If a broker becomes unavailable, the affected partition's availability becomes the important question.

---

# 21. Replication

Kafka can replicate partitions across brokers.

Example:

```text
Partition 0

Broker 1 → Leader
Broker 2 → Replica
Broker 3 → Replica
```

If Broker 1 fails:

```text
Broker 1
    X
  DOWN

Broker 2
    ↓
New Leader
```

Kafka can elect another eligible replica as the new leader.

Then:

```text
Producer
   |
   v
Broker 2
   |
   v
Partition 0
```

The Producer learns the updated metadata and can continue sending to the new leader.

---

# 22. If All Replicas Are Unavailable

Suppose:

```text
Partition 0

Broker 1 → DOWN
Broker 2 → DOWN
Broker 3 → DOWN
```

Then the partition is unavailable.

```text
Producer
    |
    v
Partition 0
    X
Unavailable
```

Producer retries according to its configuration.

If availability isn't restored within the relevant timeout/retry limits, the send eventually fails.

---

# 23. Kafka Cluster

A Kafka cluster consists of multiple brokers.

Example:

```text
             Kafka Cluster

      +------------------------+
      |                        |
      |   Broker 1             |
      |   Broker 2             |
      |   Broker 3             |
      |                        |
      +------------------------+
```

Topics and partitions are distributed among these brokers.

Example:

```text
Topic: transactions

Partition 0 → Broker 1
Partition 1 → Broker 2
Partition 2 → Broker 3
```

With replication:

```text
Partition 0:
    Broker 1 → Leader
    Broker 2 → Replica
    Broker 3 → Replica
```

---

# 24. ZooKeeper vs KRaft

Older Kafka architecture used ZooKeeper.

Conceptually:

```text
             ZooKeeper
                 |
        Cluster coordination
                 |
        +--------+--------+
        |        |        |
     Broker1  Broker2  Broker3
```

ZooKeeper participated in Kafka cluster metadata and controller coordination.

---

# 25. Modern Kafka — KRaft

Modern Kafka uses KRaft.

KRaft means:

```text
Kafka Raft
```

Kafka uses its own controller quorum for metadata management and consensus instead of depending on ZooKeeper.

Conceptually:

```text
              Kafka Cluster

      +--------------------------+
      |      KRaft Controllers   |
      |                          |
      | Controller 1             |
      | Controller 2             |
      | Controller 3             |
      +------------+-------------+
                   |
             Cluster Metadata
                   |
        +----------+----------+
        |          |          |
     Broker 1   Broker 2   Broker 3
```

---

# 26. What Does KRaft Manage?

KRaft is involved in managing Kafka cluster metadata and coordination.

For example:

```text
KRaft
 |
 +-- Broker membership
 |
 +-- Topic metadata
 |
 +-- Partition metadata
 |
 +-- Partition assignments
 |
 +-- Partition leadership
 |
 +-- Leader elections
 |
 +-- Cluster metadata
```

KRaft does NOT mean:

```text
KRaft stores your actual application messages.
```

Your actual Kafka records are stored in the brokers' partition logs.

Think:

```text
KRaft
   ↓
"Who is the leader?"
"Which broker owns this partition?"
"What topics/partitions exist?"
"Which metadata is current?"

Broker
   ↓
"Store the actual records."
```

---

# 27. KRaft During Broker Failure

Before failure:

```text
Partition 0

Broker 1 → Leader
Broker 2 → Replica
Broker 3 → Replica
```

Broker 1 crash:

```text
Broker 1
    X
  DOWN
```

KRaft controller quorum coordinates the metadata change and a new leader can be selected from eligible replicas.

For example:

```text
Partition 0

Broker 2 → New Leader
Broker 3 → Replica
```

Producer eventually learns the updated metadata:

```text
Producer
   |
   v
Broker 2
   |
   v
Partition 0
```

---

# 28. Complete End-to-End Mental Model

This is the main diagram to remember:

```text
                 PRODUCER APPLICATION
                         |
                         |
                  Java Object / JSON
                         |
                         v
                    Serializer
                         |
                         v
                       bytes
                         |
                         v
                +----------------+
                | Kafka Producer |
                +----------------+
                         |
                         v
                    Partitioner
                         |
                         v
                +----------------------+
                |     KAFKA CLUSTER    |
                |                      |
                |  KRaft Controllers   |
                |        |             |
                |        v             |
                |   Cluster Metadata   |
                |                      |
                |  +----------------+  |
                |  | Broker 1       |  |
                |  |                |  |
                |  | Partition 0    |  |
                |  +----------------+  |
                |                      |
                |  +----------------+  |
                |  | Broker 2       |  |
                |  |                |  |
                |  | Partition 1    |  |
                |  +----------------+  |
                |                      |
                |  +----------------+  |
                |  | Broker 3       |  |
                |  |                |  |
                |  | Partition 2    |  |
                |  +----------------+  |
                +----------+-----------+
                           |
                           v
                      Disk / Log
                           |
                           v
                       Consumer
                           |
                           v
                    Deserializer
                           |
                           v
                    Java Object
                           |
                           v
                 CONSUMER APPLICATION
```

---

# 29. Memory Visualization

Keep this mental model for memory:

```text
                 PRODUCER APP
                     |
                 JVM Memory
                     |
              Producer Buffer
                     |
                     v
                  Network
                     |
                     v
              KAFKA BROKER
                     |
             OS Page Cache
                     |
                     v
                   Disk
                     |
              Kafka Log Files
                     |
                     v
             Consumer Fetch
                     |
              Consumer Buffer
                     |
                 JVM Memory
                     |
                     v
             CONSUMER APP
```

The important point:

```text
Producer Buffer
      ≠
Kafka Storage
```

Data sitting in Producer memory has not necessarily reached durable Kafka storage yet.

---

# 30. Complete Example

Suppose a Banking application sends:

```json
{
  "transactionId": "TXN1001",
  "amount": 5000,
  "status": "SUCCESS"
}
```

### Step 1 — Application

```text
Transaction Object
```

### Step 2 — Serializer

```text
Transaction Object
       |
       v
JSON Serializer
       |
       v
bytes
```

### Step 3 — Producer

```text
Producer
   |
   v
Partitioner
```

Suppose it chooses:

```text
Partition 2
```

### Step 4 — Kafka

```text
Topic: transactions

Partition 2

Offset 100
    |
    +-- Key: TXN1001
    +-- Value: bytes
```

Kafka appends the record to the partition log.

### Step 5 — Consumer

```text
Consumer
    |
    | fetch offset 100
    v
Kafka
    |
    v
bytes
```

### Step 6 — Deserialization

```text
bytes
  |
  v
JSON Deserializer
  |
  v
Transaction Object
```

### Step 7 — Business Logic

```text
Transaction Object
       |
       v
Reconciliation Service
```

---

# 31. Final Mental Model

Remember these 10 points:

```text
1. Producer produces/sends records.

2. Producer serializes data into bytes.

3. Kafka stores records as bytes.

4. A Topic is divided into Partitions.

5. Each Partition is an ordered append-only log.

6. Each record gets an Offset within its Partition.

7. Consumer fetches records from Kafka.

8. Consumer deserializes bytes back into useful data.

9. Kafka does not delete a record merely because a Consumer reads it.

10. KRaft manages Kafka cluster metadata and coordination;
    Brokers store the actual Kafka records.
```

The complete flow is:

```text
Application
    ↓
Producer
    ↓
Serializer
    ↓
Bytes
    ↓
Partition
    ↓
Kafka Broker
    ↓
Append-only Log
    ↓
Disk
    ↓
Consumer Fetch
    ↓
Bytes
    ↓
Deserializer
    ↓
Application Object
```

For your Kafka learning sequence, the next important layer after these fundamentals is:

```text
Producer
   ↓
acks=0 / 1 / all
   ↓
Replication Factor
   ↓
Leader + Followers
   ↓
ISR
   ↓
Offset & Commit
   ↓
Consumer Group
   ↓
Balancing
```

These concepts explain **how Kafka provides durability, fault tolerance, and reliable consumption**.
