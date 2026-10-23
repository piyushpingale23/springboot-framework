# Apache Kafka — Session 1 Notes

## 1. What is Kafka?

Apache Kafka is a **distributed event streaming platform** used to:

* Exchange data between applications
* Decouple microservices
* Process high volumes of data

A simple mental model:

```text
Producer
    |
    v
  Kafka
    |
    v
Consumer
```

More commonly:

```text
Producer
    |
    v
Kafka Topic
    |
    +----> Consumer A
    |
    +----> Consumer B
    |
    +----> Consumer C
```

Kafka acts as a durable middle layer between producers and consumers.

---

# 2. Where did Kafka come from?

Kafka was originally developed at **LinkedIn** around 2010.

LinkedIn had a requirement to process a huge amount of user activity data and distribute that data to many different systems.

For example:

```text
User Activity
      |
      +----> Search
      |
      +----> Analytics
      |
      +----> Recommendations
      |
      +----> Monitoring
```

Instead of tightly coupling every application with every other application, Kafka was developed as a central event/data streaming system.

Kafka was open-sourced in 2011 and later became an Apache project.

The name Kafka was inspired by the writer **Franz Kafka**.

---

# 3. Why do we need Kafka?

Consider direct communication:

```text
Payment Service
      |
      +----> Recon Service
      |
      +----> Notification Service
      |
      +----> Audit Service
```

This creates several problems.

## 3.1 Tight Coupling

Payment Service needs to know about all downstream services.

```text
Payment
   |
   +--> Recon
   +--> Audit
   +--> Notification
```

If another service is added, Payment Service may need to change.

---

## 3.2 Service Availability

Suppose Notification Service is down:

```text
Payment Service
      |
      X
Notification Service
```

Depending on the implementation, Payment Service may have to:

* Wait
* Retry
* Fail
* Handle timeout

---

## 3.3 Traffic Bursts

Suppose millions of transactions arrive within a short period:

```text
10:00 AM

Txn1
Txn2
Txn3
...
Txn1000000
```

Downstream applications may not be able to process all transactions immediately.

Kafka can act as a buffer:

```text
Producer
   |
   v
Kafka
   |
   v
Consumer
```

The producer and consumer don't have to process the data at exactly the same speed.

---

## 3.4 Durability

Kafka stores records for a configured retention period.

If a consumer is temporarily unavailable:

```text
Producer
   |
   v
Kafka
   |
   X
Consumer DOWN
```

The records can remain in Kafka.

When the consumer comes back:

```text
Consumer UP
   |
   v
Kafka
```

It can consume the previously stored records.

---

## 3.5 Replay

Kafka allows consumers to read previously stored records again.

This is a major Kafka feature.

Example:

```text
Kafka

Offset 0 -> Txn1
Offset 1 -> Txn2
Offset 2 -> Txn3
Offset 3 -> Txn4
```

A consumer can potentially go back and process earlier records again.

---

## 3.6 Scalability

Kafka can distribute data across multiple partitions and brokers.

This allows:

* Parallel processing
* Horizontal scaling
* High throughput

---

# 4. High-Level Kafka Architecture

```text
                         Kafka Cluster
                              |
                 +------------+------------+
                 |            |            |
              Broker 1     Broker 2     Broker 3
                 |
                 |
             Kafka Topic
                 |
        +--------+--------+
        |        |        |
        v        v        v
    Consumer  Consumer  Consumer
```

A more realistic architecture:

```text
                    Producer
                       |
                       |
                       v
              +----------------+
              |  Kafka Topic   |
              +----------------+
                 |    |    |
                 v    v    v
                P0   P1   P2
                 |    |    |
                 +----+----+
                      |
              Consumer Group
                 /    |    \
                C1    C2    C3
```

---

# 5. Kafka Core Components

The important Kafka concepts are:

1. Producer
2. Consumer
3. Broker
4. Cluster
5. Topic
6. Partition
7. Offset
8. Consumer Group
9. ZooKeeper / KRaft

---

## 5.1. Producer

A **Producer** is an application that sends records to Kafka.

Example:

```text
Payment Service
      |
      v
    Kafka
```

Suppose Payment Service generates:

```json
{
  "transactionId": "TXN123",
  "amount": 5000,
  "status": "SUCCESS"
}
```

The application can explicitly publish this record to Kafka.

Example Spring Boot:

```
kafkaTemplate.send("transaction-events",transaction);
```

The Producer is responsible for things such as:

* Creating records
* Selecting the topic
* Determining/using a partition
* Sending records to Kafka
* Handling acknowledgements
* Handling retries

---

## 5.2. Consumer

A **Consumer** is an application that reads records from Kafka.

Example:

```text
Kafka
  |
  v
Recon Service
```

The Recon Service can consume records from:

```text
transaction-events
```

The Consumer is responsible for:

* Reading records
* Processing records
* Tracking its position
* Committing offsets

---

## 5.3. Broker

A **Broker is a Kafka server**.

Think of it as:

```text
Spring Boot Application
        |
        v
   Tomcat Server
```

Similarly:

```text
Kafka Application
        |
        v
    Kafka Broker
```

A broker stores Kafka records and handles requests from producers and consumers.

Example:

```text
Broker-1
Broker-2
Broker-3
```

---

## 5.4. Cluster

A **Kafka Cluster** is a collection of Kafka brokers working together.

Example:

```text
Kafka Cluster

+-------------+
|  Broker 1   |
+-------------+

+-------------+
|  Broker 2   |
+-------------+

+-------------+
|  Broker 3   |
+-------------+
```

A cluster provides:

* Scalability
* Fault tolerance
* Replication
* High availability

Instead of depending on one Kafka server:

```text
Kafka Cluster
   |
   +--> Broker 1
   +--> Broker 2
   +--> Broker 3
```

---

## 5.5. Topic

A **Topic** is a logical category/name to which records are published.

Examples:

```text
transaction-events
recon-events
audit-events
notification-events
```

A Producer publishes records to a topic:

```text
Payment Service
      |
      v
transaction-events
```

Consumers consume records from a topic:

```text
transaction-events
      |
      +----> Recon Service
      +----> Audit Service
      +----> Reporting Service
```

A topic is a logical concept. It is not simply equivalent to one physical file.

A topic is divided into partitions.

---

## 5.6. Partition

A **Partition** is a subdivision of a Kafka topic.

Example:

```text
Topic: transaction-events

        +----------------+
        | Partition 0    |
        +----------------+

        +----------------+
        | Partition 1    |
        +----------------+

        +----------------+
        | Partition 2    |
        +----------------+
```

Instead of storing everything in one sequential stream, Kafka distributes the topic across partitions.

Example:

```text
Partition 0:

Txn1
Txn4
Txn7
```

```text
Partition 1:

Txn2
Txn5
Txn8
```

```text
Partition 2:

Txn3
Txn6
Txn9
```

Partitions provide:

* Parallelism
* Scalability
* Higher throughput

Partitions will be studied in much more detail later.

---

### 5.6.1. Partition Selection

### Can we decide which partition stores the data?

Yes. There are mainly 3 ways:

### 1. Explicit Partition

Producer can directly specify the partition:

```
new ProducerRecord<>("txn-events", 2, "TXN123", "data");
```

`2` → record goes to Partition 2.

### 2. Using a Key — Common Approach

```
new ProducerRecord<>("txn-events", "TXN123", "data");
```

Kafka uses the key to determine the partition.

```text
Key → Partitioner → Partition

TXN123 → P1
TXN456 → P2
```

The same key is normally sent to the same partition, so related records can remain ordered.

### 3. No Key

If no key is provided, the producer's partitioner decides how to distribute the records.

---

## 5.7. Offset

An **Offset** represents the position of a record within a partition.

Example:

```text
Partition 0

Offset 0 -> Txn1
Offset 1 -> Txn2
Offset 2 -> Txn3
Offset 3 -> Txn4
```

Important:

> Offset is unique only within a partition.

For example:

```text
Partition 0

Offset 0
Offset 1
Offset 2
```

and:

```text
Partition 1

Offset 0
Offset 1
Offset 2
```

are both valid.

Offsets help consumers keep track of their processing position.

Example:

```text
Last processed offset = 100
```

The consumer can continue from the next required position.

Offsets will be studied in detail later.

---

## 5.8. Consumer Group

A **Consumer Group** is a group of consumers working together to consume a topic.

Example:

```text
Consumer Group: Recon Group

        +-------------+
        | Consumer 1  |
        +-------------+

        +-------------+
        | Consumer 2  |
        +-------------+

        +-------------+
        | Consumer 3  |
        +-------------+
```

Suppose the topic has three partitions:

```text
Topic

P0
P1
P2
```

Kafka can distribute them:

```text
P0 ---> Consumer 1
P1 ---> Consumer 2
P2 ---> Consumer 3
```

This allows parallel processing.

---

## Important Consumer Group Rule

Within a single consumer group, a partition is assigned to only one consumer at a time.

Example:

```text
3 Partitions
5 Consumers

P0 ---> C1
P1 ---> C2
P2 ---> C3

C4 ---> idle
C5 ---> idle
```

Therefore:

> Increasing consumers beyond the number of partitions does not increase parallelism for that topic/group.

---

## 5.9. ZooKeeper

Historically, Kafka used **ZooKeeper** for cluster coordination and metadata management.

ZooKeeper was involved in things such as:

* Broker registration
* Cluster coordination
* Leader election
* Metadata management
* Health information

Historically:

```text
Kafka Brokers
     |
     v
 ZooKeeper
```

Modern Kafka can run using **KRaft (Kafka Raft Metadata mode)**.

Modern architecture:

```text
Kafka Cluster
     |
     +--> Broker
     +--> Broker
     +--> Broker

No ZooKeeper required
```

Therefore:

> ZooKeeper is a historical Kafka architecture component; modern Kafka uses KRaft instead.

We will study ZooKeeper vs KRaft later.

---

## 5.10. 3 Partitions and 4 Consumers

Suppose:

```text
Topic: txn-events
Partitions: P0, P1, P2

Consumer Group: recon-group
Consumers: C1, C2, C3, C4
```

Kafka can assign:

```text
P0 -> C1
P1 -> C2
P2 -> C3

C4 -> IDLE
```

Why?

Because there are only **3 partitions**, so at most **3 consumers** of the same consumer group can actively consume this topic at the same time.

## When does C4 get a chance?

If an active consumer leaves or fails:

```text
Before:

P0 -> C1
P1 -> C2
P2 -> C3
C4 -> IDLE
```

Suppose C2 crashes:

```text
After rebalance:

P0 -> C1
P1 -> C4
P2 -> C3
```

C4 now becomes active.

### Important

C4 does NOT wait for a partition to finish processing a message.

Partitions are not released after every message.

C4 gets a partition when Kafka performs a **rebalance**, typically because a consumer joins, leaves, fails, or the group membership changes.

## Remember

```text
3 Partitions + 4 Consumers
        ↓
3 active consumers
1 idle consumer

If an active consumer leaves/fails
        ↓
Kafka balances
        ↓
Idle consumer can receive the partition
```

**Rule:**

> For a single topic and consumer group, the maximum number of actively consuming consumers is limited by the number of partitions.

---

# 6. What is a Kafka Message?

When we say:

> Kafka stores messages

we mean:

> Kafka stores records explicitly sent to Kafka by Producers.

For example:

```json
{
  "transactionId": "TXN123",
  "amount": 5000,
  "status": "SUCCESS"
}
```

If App-1 explicitly publishes this record:

```text
App-1
  |
  | produce record
  v
Kafka
```

Kafka stores the record.

Kafka does NOT automatically capture everything happening inside App-1.

---

# 7. What is an Event?

An **event** represents something that happened.

Example:

```text
Transaction completed
```

can be represented as:

```json
{
  "transactionId": "TXN123",
  "amount": 5000,
  "status": "SUCCESS"
}
```

This can be called an event because it represents:

> Transaction TXN123 was successfully completed.

Other examples:

```text
Customer Registered
Payment Completed
Payment Failed
Transaction Matched
File Uploaded
Order Created
```

Important distinction:

```text
Message = data/record sent through Kafka

Event = a message/record that represents something that happened
```

An event is therefore a common type/use of Kafka message.

---

# 8. Does Kafka Store API Calls?

No.

Suppose:

```text
App-1
  |
  | REST API
  v
App-2
```

For example:

```http
POST /transactions

{
  "transactionId": "TXN123",
  "amount": 5000
}
```

Kafka is not involved.

The API request is NOT automatically stored in Kafka.

Kafka only receives data if the application explicitly publishes something to Kafka.

---

# 9. Direct API Communication vs Kafka

## Direct API

```text
App-1
  |
  | HTTP Request
  v
App-2
```

App-1 directly communicates with App-2.

This is generally request/response communication.

---

## Kafka Communication

```text
App-1
  |
  | Produce event
  v
Kafka Topic
  |
  +----> App-2
  |
  +----> App-3
  |
  +----> App-4
```

App-1 publishes an event.

Interested applications consume it.

---

# 10. Kafka Does Not Automatically Capture Application Data

This is an important mental model.

Wrong:

```text
Application
     |
     v
Kafka automatically stores everything
```

Correct:

```text
Application
     |
     | Explicitly publish
     v
Kafka Topic
     |
     v
Broker stores record
```

For example:

```
kafkaTemplate.send("transaction-events",transaction);
```

Only the data explicitly sent through this operation is published to Kafka.

---

# 11. Does App-2 Have to Store Kafka Data in DB?

No.

Kafka only delivers/stores the event.

The Consumer decides what to do with it.

Example:

```text
                     Kafka
                       |
              transaction-events
                       |
       +---------------+---------------+
       |               |               |
       v               v               v
    Recon           Audit         Notification
       |               |               |
       v               v               v
      DB              DB             SMS
```

Recon may store the event in PostgreSQL.

Audit may store an audit record.

Notification may send an SMS.

Another application could:

* Call another API
* Update a cache
* Perform calculations
* Trigger another event
* Store data in a database
* Generate a report
* Send a notification

Kafka does not dictate what the consumer must do.

---

# 12. Real Banking Example

Suppose CBS completes a transaction.

```text
CBS
 |
 | transaction completed
 v
Payment/Transaction Service
```

The service publishes:

```json
{
  "transactionId": "TXN123",
  "amount": 5000,
  "status": "SUCCESS"
}
```

to:

```text
transaction-events
```

Kafka:

```text
                    Kafka
                      |
              transaction-events
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
      Recon         Audit       Notification
        |             |             |
        v             v             v
       DB            DB            SMS
```

The producer does not need to directly call all three applications.

---

# 13. Kafka Pub-Sub Model

Kafka supports the **Publisher-Subscriber / Pub-Sub pattern**.

Example:

```text
Producer
   |
   v
Topic
   |
   +----> Consumer Group A
   |
   +----> Consumer Group B
   |
   +----> Consumer Group C
```

Multiple consumer groups can consume the same topic independently.

For example:

```text
transaction-events
       |
       +----> Recon Group
       |
       +----> Audit Group
       |
       +----> Notification Group
```

Each group maintains its own consumption progress.

---

# 14. Kafka Also Supports Queue-Like Processing

Kafka is not just Pub-Sub.

Within a consumer group, consumers share the partitions.

Example:

```text
Topic
 |
 +--> P0 ---> Consumer 1
 |
 +--> P1 ---> Consumer 2
 |
 +--> P2 ---> Consumer 3
```

This provides queue-like workload distribution.

Therefore, Kafka combines concepts of:

```text
Pub-Sub
+
Parallel Processing
+
Persistent Log
+
Replay
```

---

# 15. Important Mental Model

The most important architecture to remember at this stage is:

```text
                         Kafka Cluster
                              |
                        Kafka Topic
                              |
                 +------------+------------+
                 |            |            |
             Partition 0  Partition 1  Partition 2
                 |            |            |
                 +------------+------------+
                              |
                       Consumer Groups
                    /          |          \
                   /           |           \
                  v            v            v
              Recon         Audit      Notification
```

Producer side:

```text
Producer
   |
   | publish record
   v
Kafka Topic
```

Consumer side:

```text
Kafka Topic
   |
   | consume record
   v
Consumer
```

---

# Kafka — ZooKeeper vs KRaft

## ZooKeeper — Old Kafka Architecture

* ZooKeeper was used for **cluster coordination and metadata management**.
* It handled things like:

    * Broker registration
    * Controller/leader coordination
    * Cluster metadata
* Kafka brokers depended on ZooKeeper.

```text
Kafka Brokers
      |
      v
  ZooKeeper
```

## KRaft — Modern Kafka Architecture

**KRaft = Kafka Raft Metadata mode**

* Kafka no longer requires ZooKeeper.
* Kafka manages its own **cluster metadata and controller coordination**.
* Uses the **Raft consensus protocol**.
* Simpler architecture with fewer external dependencies.

```text
Kafka Cluster
      |
      v
   KRaft
```

## Key Difference

```text
Old Kafka: Kafka + ZooKeeper

Modern Kafka: Kafka + KRaft
```

**Remember:**

> ZooKeeper = external coordination system used by older Kafka.
> KRaft = Kafka's built-in metadata/coordination system used in modern Kafka.

---

# 16. Key Points to Remember

1. Kafka is a distributed event streaming platform.
2. Producer sends records to Kafka.
3. Consumer reads records from Kafka.
4. Broker is a Kafka server.
5. Multiple brokers form a Kafka cluster.
6. Topic is a logical category of records.
7. Topic is divided into partitions.
8. Partition provides parallelism and scalability.
9. Offset identifies a record's position within a partition.
10. Consumer Groups allow multiple consumers to share processing.
11. Multiple consumer groups can independently consume the same topic.
12. Kafka stores records explicitly produced to it.
13. Kafka does not automatically store REST API calls.
14. A Kafka event commonly represents something that happened.
15. Consumers don't have to persist events in a database.
16. ZooKeeper was used by older Kafka architectures.
17. Modern Kafka can use KRaft instead of ZooKeeper.
18. Kafka supports Pub-Sub behavior.
19. Kafka also provides queue-like workload distribution within consumer groups.
20. Kafka's persistent log and replay capability are major differences from traditional messaging systems.

---

# Current Mental Model

Think about Kafka like this:

```text
                 APPLICATION
                     |
                     |
                 PRODUCER
                     |
                     | publish record
                     v
              +--------------+
              | KAFKA TOPIC  |
              +--------------+
                     |
             +-------+-------+
             |       |       |
             v       v       v
            P0      P1      P2
             |       |       |
             +-------+-------+
                     |
          +----------+----------+
          |          |          |
          v          v          v
       Consumer   Consumer   Consumer
          |          |          |
          v          v          v
         DB         API       Analytics
```

The next important question is:

**How exactly does Kafka store a Topic using Partitions, and where are those Partitions physically stored on Broker disks?**

That will lead into Kafka's **partition architecture, leader/follower replication, segments, log files, indexes, and offsets**.
