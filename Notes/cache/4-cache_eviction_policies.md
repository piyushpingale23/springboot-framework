# Cache Eviction Policies in System Design

## 1. What is Cache Eviction?

Cache Eviction is the process of removing existing entries from a cache to make space for new entries when the cache reaches its memory limit.

### Why is it required?

- Cache memory (RAM) is limited.
- Applications continuously add new entries.
- Cache cannot store unlimited data.
- Therefore, some existing entries must be removed when space is needed.

### Example

Suppose Redis has a maximum memory allocation of 100 MB.

If the application wants to store 120 MB of data, the cache needs to remove some existing entries.

The eviction policy determines which entries should be removed.

### Common Cache Eviction Policies

1. LRU — Least Recently Used
2. LFU — Least Frequently Used
3. FIFO — First In, First Out

---

## 2. LRU (Least Recently Used)

LRU removes the cache entry that has not been accessed for the longest time.

**In simple words:** The data that was used least recently is removed first.

### Example

Suppose the cache can store only three entries.

Initially, we insert:

```text
A -> B -> C
```

Access order:

```text
A -> B -> C
```

Now, the application accesses A.

A becomes the most recently used entry.

New access order:

```text
B -> C -> A
```

Now the application wants to insert D.

The cache is full, so LRU removes B because it is the least recently accessed entry.

Final cache:

```text
C -> A -> D
```

### Important Points

- LRU considers the last access time.
- Recently accessed entries are generally retained.
- Frequently accessed data can still be removed if it has not been accessed recently.
- Useful when recently accessed data is likely to be accessed again.
- Can be implemented using a HashMap and Doubly Linked List.

### Time Complexity

A well-designed LRU implementation can achieve:

- Get: O(1) average time
- Put: O(1) average time

---

## 3. LFU (Least Frequently Used)

LFU removes the cache entry that has been accessed the fewest number of times.

**In simple words:** The data with the lowest access frequency is removed first.

### Example

Suppose the cache can store only three entries.

| Cache Entry | Access Count |
|-------------|-------------:|
| A           |           10 |
| B           |            2 |
| C           |            7 |

Now the application wants to insert D.

LFU compares the access counts:

- A has 10 accesses.
- B has 2 accesses.
- C has 7 accesses.

B has the lowest access frequency, so LFU removes B.

Final cache:

```text
A -> C -> D
```

### Important Points

- LFU considers the number of accesses.
- Frequently accessed data is generally retained.
- Useful when some data is consistently more popular than other data.
- Requires maintaining access-frequency information.
- When two entries have the same frequency, an additional tie-breaking policy is required, often LRU.

### Limitation

LFU can retain data that was very popular in the past but is no longer requested.

Some implementations use frequency aging or decay to address this problem.

---

## 4. FIFO (First In, First Out)

FIFO removes the cache entry that was inserted first.

**In simple words:** The oldest inserted data is removed first.

### Example

Suppose the cache can store only three entries.

Insertion order:

```text
A -> B -> C
```

- A was inserted first.
- B was inserted second.
- C was inserted third.

Now the application wants to insert D.

FIFO removes A because it was inserted first.

Final cache:

```text
B -> C -> D
```

### Important Points

- FIFO considers insertion time.
- It does not consider how frequently an entry is accessed.
- It does not consider how recently an entry is accessed.
- Simple to implement using a Queue.

---

## 5. LRU vs LFU vs FIFO

| Feature                   | LRU                          | LFU                                  | FIFO                |
|---------------------------|------------------------------|--------------------------------------|---------------------|
| Full form                 | Least Recently Used          | Least Frequently Used                | First In, First Out |
| Based on                  | Last access time             | Access frequency                     | Insertion time      |
| Removes                   | Least recently accessed      | Least frequently accessed            | First inserted      |
| Considers access history? | Yes, recency                 | Yes, frequency                       | No                  |
| Implementation concept    | HashMap + Doubly Linked List | Frequency counters + data structures | Queue               |
| Complexity                | Medium                       | Higher                               | Simple              |

### Fundamental Difference

**LRU:**
```text
When was the data last accessed?
```

**LFU:**
```text
How many times was the data accessed?
```

**FIFO:**
```text
When was the data inserted?
```

---

## 6. Real-World Use Cases

| Policy | Suitable Scenario                                               |
|--------|-----------------------------------------------------------------|
| LRU    | Recently accessed user sessions, application data               |
| LFU    | Consistently popular products or frequently requested resources |
| FIFO   | Simple bounded caches where insertion order is sufficient       |

The best policy depends on the application's access pattern.

---

## 7. Interview Questions

**Q1. What is Cache Eviction?**

Cache Eviction is the process of removing existing cache entries to free memory when the cache reaches its capacity.

**Q2. What is the difference between LRU and LFU?**

- LRU removes data based on the time of its last access.
- LFU removes data based on its access frequency.

**Q3. What is the difference between LRU and FIFO?**

- LRU considers the last access time.
- FIFO considers the insertion time.

**Q4. Can frequently access data be removed by LRU?**

Yes. If the data has not been accessed recently, LRU can remove it even if it was frequently accessed in the past.

**Q5. What happens when two entries have the same frequency in LFU?**

An additional tie-breaking rule is required. A common approach is to remove the least recently used entry among those with the same frequency.

---

## 8. Quick Revision

- Cache Eviction = Removing existing entries to free cache memory.
- LRU = Removes the least recently accessed entry.
- LFU = Removes the least frequently accessed entry.
- FIFO = Removes the earliest inserted entry.