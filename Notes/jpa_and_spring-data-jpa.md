# JPA & Spring Data JPA Notes

# 1. What is JPA?

**JPA (Java Persistence API)** is a Java specification that defines how Java objects should be mapped to relational databases.

JPA is **not an implementation**.

It only defines:

- Annotations
- Interfaces
- Rules

Example:

```java
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    private Long id;

    private String name;
}
```

These annotations come from **JPA**.

---

## JPA Provides Interfaces

JPA provides several interfaces, such as:

- EntityManager
- EntityTransaction
- Query
- TypedQuery
- CriteriaBuilder

However, **JPA itself does not execute SQL**.

It only defines the API.

To actually execute SQL, you need a **JPA implementation**.

Popular JPA implementations include:

- Hibernate (Most Popular)
- EclipseLink
- OpenJPA

Spring Boot uses **Hibernate** as the default JPA provider.

---

## Example

```java
@Repository
public class EmployeeRepository {

    @PersistenceContext
    EntityManager em;

    public Employee save(Employee e) {
        em.persist(e);
        return e;
    }

    public Employee get(Long id) {
        return em.find(Employee.class, id);
    }
}
```

---

# 2. What is Spring Data JPA?

Spring Data JPA is a **Spring Framework module built on top of JPA**.

It removes most of the boilerplate code.

Instead of writing repository implementations manually, you only define an interface.

Example:

```java
@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

}
```

No implementation is required.

Spring automatically generates the implementation at runtime.

---

# JPA vs Spring Data JPA

| JPA                                         | Spring Data JPA                    |
|---------------------------------------------|------------------------------------|
| Specification                               | Spring Framework Module            |
| Defines APIs                                | Implements Repository Abstraction  |
| Uses EntityManager                          | Uses EntityManager internally      |
| Need to write DAO/Repository implementation | Repository generated automatically |
| More boilerplate code                       | Very little code                   |
| Not Spring-specific                         | Built specifically for Spring      |

---

# 1. What is JPQL?

**JPQL (Java Persistence Query Language)** is the query language defined by JPA.

It looks similar to SQL, but instead of querying **database tables**, it queries **Entity classes**.

Example:

```java
@Query("SELECT e FROM Employee e WHERE e.department = :dept")
List<Employee> getEmployees(String dept);
```

Where:

- `Employee` is the **Entity class**, not the table.
- `department` is the **Java field**, not the database column.

Hibernate converts JPQL into SQL.

---

## Advantages of JPQL

- Database independent
- Works with:
    - PostgreSQL
    - Oracle
    - MySQL
    - SQL Server
- No code changes are required when switching databases.
- Hibernate Dialect automatically generates database-specific SQL.

---

# 2. What is Native Query?

A **Native Query** means writing actual SQL.

Spring sends the SQL directly to the database.

Native Queries use:

- Database table names
- Database column names

Example:

```java
@Query(
    value = "SELECT * FROM employee WHERE department = ?",
    nativeQuery = true
)
List<Employee> getEmployees(String department);
```

---

# JPQL vs Native Query

| Feature                          | JPQL                   | Native Query                       |
|----------------------------------|------------------------|------------------------------------|
| Query Language                   | JPQL                   | SQL                                |
| Uses Entity Class                | Yes                    | No                                 |
| Uses Java Fields                 | Yes                    | No                                 |
| Uses Database Tables             | No                     | Yes                                |
| Uses Database Columns            | No                     | Yes                                |
| Database Independent             | Yes                    | No                                 |
| Supports Vendor-Specific SQL     | No                     | Yes                                |
| Supports CTEs                    | No                     | Yes                                |
| Supports Window Functions        | No                     | Yes                                |
| Supports Stored Procedures       | Limited (`@Procedure`) | Yes                                |
| Automatically Mapped to Entities | Yes                    | Yes (if result matches the entity) |

---

# 1. Hibernate Converts JPQL into SQL

## Question

**Is the JDBC Driver responsible for converting JPQL into SQL?**

## Answer

**No.**

The **Hibernate JPA provider** is responsible for converting **JPQL** into **SQL**.

The **JDBC Driver does not understand JPQL**.

---

## Example

```java
@Query("SELECT e FROM Employee e WHERE e.salary > :salary")
List<Employee> getEmployees(double salary);
```

---

## Step 1: Hibernate Parses JPQL

Hibernate understands the Entity mapping.

```text
Employee
        ↓
employees table

salary
        ↓
salary column
```

Hibernate reads the metadata provided by the JPA annotations.

For example:

```java
@Entity
@Table(name = "employees")
public class Employee {

    @Column(name = "salary")
    private Double salary;
}
```

Using this metadata, Hibernate knows:

- Entity → Table
- Java field → Database column

---

## Step 2: Hibernate Generates SQL

For PostgreSQL:

```sql
SELECT
    e.id,
    e.name,
    e.salary
FROM employees e
WHERE e.salary > ?;
```

For Oracle, Hibernate generates Oracle-compatible SQL.

For MySQL, Hibernate generates MySQL-compatible SQL.

The generated SQL depends on the configured Hibernate Dialect.

---

## Step 3: JDBC Driver Executes SQL

The PostgreSQL JDBC Driver receives SQL like:

```sql
SELECT
    e.id,
    e.name,
    e.salary
FROM employees e
WHERE e.salary > ?;
```

The JDBC Driver **does not translate JPQL**.

Its responsibilities are:

- Open a database connection
- Send SQL to the database
- Receive the ResultSet
- Convert database values into Java types

---

## Complete Flow

```text
JPQL
        │
        ▼
Hibernate Query Parser
        │
        ▼
Entity Metadata
(@Entity, @Table, @Column)
        │
        ▼
Hibernate SQL Generator
        │
        ▼
Database-specific SQL
        │
        ▼
JDBC Driver
        │
        ▼
Database
```

---

# Responsibilities

## Hibernate

Responsible for:

- Parsing JPQL
- Mapping Entity classes to database tables
- Mapping Java fields to database columns
- Generating SQL
- Using the configured Hibernate Dialect

---

## JDBC Driver

Responsible for:

- Opening database connections
- Sending SQL
- Receiving results
- Converting database data into Java objects

---

# 2. Should We Always Use JPQL?

## Answer

**Generally, Yes.**

JPQL should be your **first choice** for most application queries because it is:

- Database independent
- Easier to maintain
- Object-oriented
- Works with Entity classes instead of tables

---

## Example

Suppose today you're using PostgreSQL.

```java
@Query("SELECT e FROM Employee e")
List<Employee> getEmployees();
```

Hibernate generates PostgreSQL SQL.

---

Later, your application migrates to Oracle.

You **do not change the JPQL**.

```java
@Query("SELECT e FROM Employee e")
List<Employee> getEmployees();
```

Hibernate automatically generates Oracle-compatible SQL.

Your repository code remains unchanged.

---

## When Should Native Query Be Used?

Use Native Query when you need database-specific features such as:

- CTE (Common Table Expressions)
- Window Functions
- Recursive Queries
- Database-specific functions
- Optimizer hints
- Complex reporting queries

Otherwise, prefer JPQL.

---

# 3. Is Hibernate Dialect Responsible for Converting JPQL into SQL?

## Answer

**Not directly.**

Hibernate itself parses and converts JPQL (or HQL) into SQL using its query engine.

The **Hibernate Dialect** provides the database-specific SQL rules.

---

## Responsibilities of Hibernate Dialect

The Dialect tells Hibernate how to generate SQL for a specific database.

Examples include:

- Pagination syntax
- SQL functions
- Identifier quoting
- Data type mappings
- Database-specific keywords

---

### Example

JPQL:

```
@Query("SELECT e FROM Employee e");
```

Hibernate generates different SQL depending on the Dialect.

For PostgreSQL:

```sql
SELECT *
FROM employees
LIMIT 10;
```

For Oracle:

```sql
SELECT *
FROM employees
FETCH FIRST 10 ROWS ONLY;
```

The JPQL remains exactly the same.

Only the generated SQL changes.

---

## Hibernate Query Generation Flow

```text
JPQL
        │
        ▼
Hibernate Query Engine
        │
        ▼
Hibernate Dialect
(Database-specific rules)
        │
        ▼
Database-specific SQL
```

---

# 4. Are JPQL and HQL the Same?

## Answer

JPQL and HQL have **almost identical syntax**, but they are **not exactly the same**.

---

## JPQL

JPQL is the **standard query language** defined by the **JPA Specification**.

It works with **any JPA-compliant provider**, including:

- Hibernate
- EclipseLink
- OpenJPA

---

## HQL

HQL (**Hibernate Query Language**) is Hibernate own query language.

Hibernate implements JPQL and extends it with Hibernate-specific features.

Therefore:

Every JPQL query is valid HQL.

However, some HQL features are not part of the JPA specification.

---

## Example

Standard JPQL:

```
SELECT e FROM Employee e WHERE e.salary > :salary
```

Hibernate executes it without any issue.

---

## In Spring Boot

Since Spring Boot uses **Hibernate** as the default JPA provider, the JPQL queries you write are actually executed by Hibernate.

Therefore, many developers use the terms **JPQL** and **HQL** interchangeably.

However:

- JPQL = JPA Standard
- HQL = Hibernate Extension

---

# JPQL vs HQL

| Feature                     | JPQL              | HQL                                             |
|-----------------------------|-------------------|-------------------------------------------------|
| Defined By                  | JPA Specification | Hibernate                                       |
| Standard                    | Yes               | No                                              |
| Database Independent        | Yes               | Yes                                             |
| Works with Entity Classes   | Yes               | Yes                                             |
| Hibernate-specific Features | No                | Yes                                             |
| Portability                 | High              | Lower (if Hibernate-specific features are used) |

---

# Key Takeaways

- Hibernate converts JPQL into SQL.
- The JDBC Driver never converts JPQL into SQL.
- The JDBC Driver only executes SQL.
- Hibernate Dialect provides database-specific SQL rules.
- JPQL should be the default choice for most queries.
- Native Queries should be used only when JPQL cannot express the required SQL.
- JPQL is the JPA standard query language.
- HQL is Hibernate implementation and extension of JPQL.