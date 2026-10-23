# JDBC Notes

## 1. DriverManager

DriverManager is responsible for finding the correct JDBC Driver.

---

## 2. Driver

A JDBC Driver is a Java library that allows Java applications to communicate with a specific database.

A JDBC Driver is only responsible for communicating between the Java application and the database. If we change the database (for example, from MySQL to Oracle), we only need to change the JDBC Driver.

---

## 3. URL (Database URL)

The URL tells JDBC:

- Which database to connect to
- Where the database is running
- Which port to use
- Which database/schema to access

---

## 4. Dialect (Hibernate/JPA Concept)

Dialect is a Hibernate/JPA component that tells Hibernate how to generate database-specific SQL for a particular database.

---

# What is JDBC?

- JDBC (Java Database Connectivity) is a standard Java API that allows Java applications to communicate with relational databases such as MySQL, PostgreSQL, Oracle Database, and Microsoft SQL Server.
- JDBC uses JDBC Drivers for connection.
- JDBC provides a common interface so that Java code can work with different databases without changing much application code.

---

# JDBC Architecture

```text
Java Application
      |
      v
   JDBC API
      |
      v
JDBC DriverManager
      |
      v
 JDBC Driver
      |
      v
   Database
```

---

# Components of JDBC

1. DriverManager
2. JDBC Driver
3. JDBC API Interfaces
    - Connection
    - Statement
    - PreparedStatement
    - ResultSet

---

# Example Flow

```
Connection con = DriverManager.getConnection(url, username, password);
PreparedStatement ps = con.prepareStatement("select * from employee where id=?");
ps.setInt(1, 101);
ResultSet rs = ps.executeQuery();
```

---

# Internal Working (Step-by-Step)

### Step 1: Driver Loading

```
Class.forName("com.mysql.cj.jdbc.Driver");
```

Class Loader loads MySQL Driver Class.

Internal DriverManager List:

```text
+-------------------+
| MySQL Driver      |
| Oracle Driver     |
| PostgreSQL Driver |
+-------------------+
```

### Step 2: Connection Creation

```java
Connection con =
    DriverManager.getConnection(url, user, password);
```

### Step 3: Statement Creation

```java
Statement st = con.createStatement();
```

### Step 4: SQL Execution

```java
ResultSet rs =
    st.executeQuery("select * from employee");
```

### Step 5: ResultSet Creation

Database returns rows, and the Driver converts them into a ResultSet.

---

# 1. What is Connection?

Connection is a JDBC interface responsible for database connection and represents an active session between a Java application and a database.

```java
Connection con = DriverManager.getConnection(url, user, password);
```

## Why Do We Need a Connection?

1. Locate the database server.
2. Open a TCP connection.
3. Authenticate the user.
4. Create a session.
5. Maintain transaction state.

All of this is represented by the Connection object.

---

# Step-by-Step Internal Working

### Step 1

Application calls:

```
DriverManager.getConnection(url, username, password);
```

DriverManager finds the correct JDBC Driver.

---

### Step 2

DriverManager identifies the suitable Driver.

Example URL:

```text
jdbc:mysql://localhost:3306/company
```

Prefix:

```text
jdbc:mysql:
```

indicates MySQL Driver.

---

### Step 3

Driver creates a Socket connection.

---

### Step 4

Authentication

Driver sends:

```text
Username = root
Password = password
```

Database verifies credentials.

- Valid → Authentication Success
- Invalid → SQLException

---

### Step 5

Database creates a session.

---

### Step 6

Driver returns a Connection object.

Example implementation:

```text
Connection
      ↓
MySQLConnection
```

---

# What Can We Do Using Connection?

1. Create Statement
2. Create PreparedStatement
3. Create CallableStatement
4. Commit Transaction
5. Rollback Transaction
6. Close Connection

---

# Lifecycle of a Connection

```text
getConnection()
      |
      v
Authentication
      |
      v
Database Session Created
      |
      v
Create Statement
      |
      v
Execute SQL
      |
      v
Commit / Rollback
      |
      v
Close Connection
```

---

# 2. What is Statement?

After a Connection is established, the next step is to execute SQL.

Statement is a JDBC interface used to execute static SQL queries.

Example:

```java
Connection con = DriverManager.getConnection(url, username, password);

Statement st = con.createStatement();

ResultSet rs = st.executeQuery("SELECT * FROM employee");
```

---

## Why Do We Need Statement?

- A Connection only establishes communication with the database.
- No SQL has been executed yet.
- To execute a static SQL query, we need a Statement.

---

## Internal Flow

```java
Statement st = con.createStatement();
```

No SQL is executed yet.

The JDBC Driver simply creates a Statement object associated with that Connection.

---

## What Happens During createStatement()?

```java
Statement st = con.createStatement();
```

### Step 1

Connection verifies whether a database session exists.

If no session exists, a `SQLException` is thrown.

### Step 2

The JDBC Driver creates a Statement implementation.

Example:

```text
Statement
     ↓
MySQLStatement
```

---

## Executing SQL

```java
ResultSet rs = st.executeQuery("SELECT * FROM employee");
```

### Internal Steps

**Step 1**

Application sends SQL to Statement.

↓

**Step 2**

Statement passes SQL to JDBC Driver.

↓

**Step 3**

Driver converts the SQL request into database protocol packets.

↓

**Step 4**

Driver sends packets through the socket connection.

↓

**Step 5**

Database parses and executes the SQL.

↓

**Step 6**

Database sends the result back.

↓

Driver converts the raw data into a ResultSet.

---

## Important Methods of Statement

### 1. executeQuery(sql)

Used for **SELECT** operations.

```java
ResultSet rs =
        st.executeQuery("SELECT * FROM employee");
```

---

### 2. executeUpdate(sql)

Used for:

- INSERT
- UPDATE
- DELETE

```java
int count =
    st.executeUpdate("DELETE FROM employee WHERE id=1");
```

`count` = Number of rows affected.

---

### 3. execute(sql)

Used when you don't know whether the SQL returns rows or an update count.

```java
boolean result = st.execute(sql);
```

Returns:

- `true` → ResultSet
- `false` → Update Count

---

## Drawbacks of Statement

### 1. SQL Injection

Statement does not support parameter binding.

Dynamic values are concatenated directly into the SQL string, making the application vulnerable to SQL Injection attacks.

---

### 2. Performance

Every SQL statement is parsed and compiled by the database on every execution.

This reduces performance for frequently executed queries.

---

### 3. Limited Use for Dynamic Queries

Statement is best suited for static SQL.

It is not recommended for queries that accept user input.

---

# Connection vs Statement

| Feature                  | Connection    | Statement   |
|--------------------------|---------------|-------------|
| Purpose                  | Connect to DB | Execute SQL |
| Creates Session          | Yes           | No          |
| Executes SQL             | No            | Yes         |
| Creates ResultSet        | No            | Yes         |
| Communicates with Driver | Yes           | Yes         |

---

# 3. What is PreparedStatement?

Internally, both Statement and PreparedStatement eventually send SQL to the database through the JDBC Driver.

The major difference is **when** and **how** SQL is parsed and how parameter values are handled.

```text
Statement
        ↓
Parse & Compile during executeQuery(sql)
(Parsing while execution)

PreparedStatement
        ↓
Parse & Compile during prepareStatement(sql)
(Parsing while creation)
```

---

# Why Was PreparedStatement Introduced?

Suppose we use Statement.

```java
String sql =
        "SELECT * FROM employee WHERE id = " + id;

Statement st = con.createStatement();

ResultSet rs = st.executeQuery(sql);
```

---

## Problem 1: SQL Injection

If the user enters:

```text
1 OR 1=1
```

The SQL becomes:

```sql
SELECT * FROM employee WHERE id = 1 OR 1=1;
```

Since `1=1` is always true, the query may return every row in the table.

Therefore, Statement is vulnerable to SQL Injection because user input is directly concatenated into the SQL.

---

## Problem 2: Repeated Parsing & Compilation

```
for (int i = 1; i <= 1000; i++) {
    String sql = "SELECT * FROM employee WHERE id = " + i;
    st.executeQuery(sql);
}
```

The database receives:

```sql
SELECT * FROM employee WHERE id = 1

SELECT * FROM employee WHERE id = 2

SELECT * FROM employee WHERE id = 3

...
```

Although only the value changes, the database treats every SQL statement as a new query.

For every execution, the database performs:

```text
Receive SQL
      ↓
Parse SQL
      ↓
Validate Syntax
      ↓
Check Table / Column Existence
      ↓
Create Execution Plan
      ↓
Execute Query
      ↓
Return Result
```

This repeated parsing and compilation reduces performance.

---

# Solution: PreparedStatement

PreparedStatement solves both problems.

Instead of sending:

```sql
SELECT * FROM employee WHERE id = 101
```

it sends:

```sql
SELECT * FROM employee WHERE id = ?
```

The SQL structure remains fixed.

Only the parameter value changes at runtime.

---

# Benefits of PreparedStatement

- Prevents SQL Injection.
- Improves performance by reusing the compiled execution plan.
- Supports parameter binding using `setInt()`, `setString()`, `setLong()`, etc.
- Produces cleaner and more maintainable code.

---

# Step-by-Step Internal Working of PreparedStatement

### Step 1: Create PreparedStatement

```java
PreparedStatement ps =
    con.prepareStatement(
        "SELECT * FROM employee WHERE id = ?"
    );
```

The SQL is sent to the database only once for preparation.

---

### Step 2: Database Parses SQL Once

Database receives:

```sql
SELECT * FROM employee WHERE id = ?
```

The database performs:

- Parse SQL
- Validate SQL
- Generate Execution Plan
- Store Prepared Query

---

### Step 3: Database Returns a Statement Handle

Conceptually:

```text
Prepared Query #123
```

The database remembers:

```sql
SELECT * FROM employee WHERE id = ?
```

along with its execution plan.

---

### Step 4: Bind Parameter Values

Java Code:

```
ps.setInt(1, 101);
```

Internally:

```text
Parameter #1 = 101
```

Only the parameter value is sent.

The SQL structure does not change.

---

### Step 5: Execute Query

```java
ResultSet rs = ps.executeQuery();
```

The database executes the already prepared execution plan.

---

### Step 6: Database Reuses Existing Execution Plan

Since the SQL has already been parsed and compiled, the database skips:

- Parsing
- Validation
- Execution Plan Generation

It directly executes the query using the supplied parameter.

---

# Multiple Executions

```
PreparedStatement ps = con.prepareStatement("SELECT * FROM employee WHERE id = ?");
ps.setInt(1, 101);
ps.executeQuery();

ps.setInt(1, 102);
ps.executeQuery();

ps.setInt(1, 103);
ps.executeQuery();
```

Internally:

```text
Prepare Once
      ↓
Parse Once
      ↓
Compile Once
      ↓
Execution Plan Created
      ↓
Execute with 101
      ↓
Execute with 102
      ↓
Execute with 103
```

Only the parameter value changes.

The execution plan is reused every time.

---

# Commonly Used PreparedStatement Methods

## Parameter Binding Methods

```
setInt()
setString()
setLong()
setDouble()
setBoolean()
setDate()
setTimestamp()
```

---

## Execution Methods

```
executeQuery()
executeUpdate()
execute()
```

---

## Batch Processing Methods

### addBatch()

Adds the current parameter set to the batch.

```
ps.addBatch();
```

---

### executeBatch()

Executes all batched SQL statements.

```
ps.executeBatch();
```

---

### clearBatch()

Removes all statements from the current batch.

```
ps.clearBatch();
```

---

# Statement

Statement is generally intended for static SQL, where the query is already complete and no values need to be supplied later.

Example:

```java
Statement st = con.createStatement();
ResultSet rs = st.executeQuery("SELECT * FROM employee");
```

Here, the SQL is fixed.

No runtime parameters are required.

---

# PreparedStatement

PreparedStatement is used for dynamic SQL, where some values are supplied at runtime.

Example:

```
PreparedStatement ps = con.prepareStatement("SELECT * FROM employee WHERE id = ?");
ps.setInt(1, 101);
ResultSet rs = ps.executeQuery();
```

The SQL structure remains fixed, but the parameter value changes.

---

# Statement vs PreparedStatement

| Feature               | Statement                     | PreparedStatement                  |
|-----------------------|-------------------------------|------------------------------------|
| Purpose               | Executes static SQL           | Executes parameterized SQL         |
| SQL Provided          | During execution              | During creation                    |
| SQL Syntax            | Complete SQL string           | SQL with `?` placeholders          |
| Parameter Binding     | Not supported                 | Supported using `setXXX()` methods |
| SQL Injection         | Vulnerable                    | Prevented                          |
| Parsing & Compilation | Every execution               | Once, then reused                  |
| Performance           | Slower for repeated execution | Faster for repeated execution      |
| Query Reusability     | No                            | Yes                                |
| Runtime Values        | Requires string concatenation | Uses parameter binding             |
| Recommended Usage     | Rarely used                   | Most commonly used                 |

---

# JDBC ResultSet

After a `Connection` establishes communication with the database and a `Statement` or `PreparedStatement` executes a SQL query, the database returns data.

That returned data is represented by the **ResultSet** interface.

---

# 1. What is ResultSet?

`ResultSet` is a JDBC interface that represents the table of data returned by a SQL **SELECT** query.

---

# 2. Why Do We Need ResultSet?

The database returns data in its own internal format.

Java cannot directly work with those records.

The JDBC Driver converts the database response into a Java object called **ResultSet**, allowing Java code to read rows and columns.

```java
ResultSet rs = ps.executeQuery();
```

---

# 3. Cursor Concept

The most important concept in ResultSet is the **Cursor**.

When a ResultSet is created:

```java
ResultSet rs = st.executeQuery(sql);
```

the cursor is positioned **before the first row**.

```text
Before First Row
        |
        v
+-----------------------+
| id | name | salary    |
+-----------------------+
|101 | John | 50000     |
|102 | Mike | 60000     |
|103 | Alex | 70000     |
+-----------------------+
```

Calling:

```
rs.next();
```

moves the cursor to the next row.

- Returns **true** if a row exists.
- Returns **false** if there are no more rows.

Example:

```
while (rs.next()) {

    int id = rs.getInt("id");

    String name = rs.getString("name");

    System.out.println(id + " " + name);
}
```

---

# 4. Reading Column Values

Columns can be accessed by **column name** or **column index**.

By Column Name:

```java
int id = rs.getInt("id");

String name = rs.getString("name");
```

By Column Index:

```java
int id = rs.getInt(1);

String name = rs.getString(2);
```

Other commonly used methods:

```java
getString();

getLong();

getDouble();

getFloat();

getBoolean();

getDate();

getTimestamp();

getObject();
```

---

# Methods of ResultSet

## Navigation Methods

```java
next();

previous();

first();

last();

beforeFirst();

afterLast();

absolute(int row);

relative(int rows);
```

---

## Data Retrieval Methods

```java
getInt();

getString();

getLong();

getDouble();

getFloat();

getBoolean();

getDate();

getTimestamp();

getObject();
```

---

# Example

```
Connection con = DriverManager.getConnection(url, user, password);
PreparedStatement ps = con.prepareStatement("SELECT * FROM employee");
ResultSet rs = ps.executeQuery();
while (rs.next()) {
    System.out.println(
        rs.getInt("id") + " " +
        rs.getString("name")
    );
}
```

---

# JDBC Transactions

A **Transaction** is a group of SQL statements executed as a **single unit of work**.

A transaction follows the **ACID** properties:

- Atomicity
- Consistency
- Isolation
- Durability

---

## Auto Commit

By default, JDBC runs in **Auto Commit** mode.

```java
Connection con =
        DriverManager.getConnection(url, user, password);
```

Every SQL statement is automatically committed.

```text
Execute SQL
      ↓
Commit Automatically
```

---

## Disable Auto Commit

```
con.setAutoCommit(false);
```

This means:

1. Auto Commit is disabled.
2. Multiple SQL statements become part of one transaction.
3. Changes are permanently saved only after calling `commit()`.
4. If an error occurs, `rollback()` restores the previous state.

---

## Commit Example

```
Connection con =
        DriverManager.getConnection(url, user, password);

con.setAutoCommit(false);

PreparedStatement ps =
        con.prepareStatement(
            "UPDATE account SET balance = balance - ? WHERE id=?"
        );

ps.executeUpdate();

con.commit();
```

---

## Rollback Example

```
try {

    con.setAutoCommit(false);

    // Multiple SQL statements

    con.commit();

} catch (Exception e) {

    con.rollback();
}
```

---

## Transaction Flow

```text
Begin Transaction
        |
        v
Execute SQL 1
        |
        v
Execute SQL 2
        |
        v
Execute SQL 3
        |
        v
Commit
```

If any SQL fails:

```text
Begin Transaction
        |
        v
Execute SQL 1
        |
        v
Execute SQL 2
        |
        X
      Error
        |
        v
Rollback
```

---

# Batch Processing in JDBC

Batch Processing allows multiple SQL statements to be grouped together and sent to the database in **one request**.

Instead of executing:

```
ps.executeUpdate();

ps.executeUpdate();

ps.executeUpdate();
```

we execute them as a batch.

```
ps.addBatch();

ps.addBatch();

ps.addBatch();

ps.executeBatch();
```

---

## Why Batch Processing?

Without Batch Processing:

```text
Java
   ↓
Database

Java
   ↓
Database

Java
   ↓
Database
```

Three separate network calls.

---

With Batch Processing:

```text
Java
   ↓
Batch of SQL Statements
   ↓
Database
```

Only one network call.

---

## Example

```
PreparedStatement ps =
        con.prepareStatement(
            "INSERT INTO employee VALUES (?, ?)"
        );

for (int i = 1; i <= 1000; i++) {

    ps.setInt(1, i);

    ps.setString(2, "Employee " + i);

    ps.addBatch();
}

ps.executeBatch();
```

---

## Advantages of Batch Processing

- Fewer network round trips.
- Better performance.
- Faster bulk INSERT, UPDATE, and DELETE operations.
- Reduced database overhead.

---

## Common Batch Methods

```java
addBatch();

executeBatch();

clearBatch();
```

---

# SQLException

`SQLException` is the exception class used in JDBC to handle database-related errors.

It is thrown when an error occurs while:

- Establishing a database connection
- Executing SQL queries
- Accessing database objects

Example:

```
try {

    Connection con =
            DriverManager.getConnection(url, user, password);

} catch (SQLException e) {

    e.printStackTrace();
}
```

Common reasons for `SQLException`:

- Invalid database URL
- Wrong username or password
- SQL syntax error
- Table or column does not exist
- Connection timeout
- Network failure
- Constraint violation

---

# DataSource

`DataSource` is a JDBC interface that provides database connections to an application.

It is the preferred alternative to:

```
DriverManager.getConnection(...)
```

---

# Problems with DriverManager

Every call to:

```
DriverManager.getConnection(...)
```

creates a **new database connection**.

Creating a database connection is expensive because it requires:

- Opening a TCP socket
- Authenticating the user
- Creating a database session
- Allocating database resources

Flow:

```text
Application
      |
      v
DriverManager.getConnection()
      |
      v
Create New Connection
      |
      v
Authenticate
      |
      v
Return Connection
```

This process repeats for every request.

---

# Solution: DataSource

Instead of creating a new connection every time:

```java
Connection con =
        dataSource.getConnection();
```

DataSource usually uses **Connection Pooling**.

Flow:

```text
Application
      |
      v
DataSource
      |
      v
Connection Pool
      |
      v
Return Existing Connection
```

Instead of creating a new connection, an existing idle connection is returned.

---

# DriverManager vs DataSource

| Feature             | DriverManager                | DataSource              |
|---------------------|------------------------------|-------------------------|
| Connection Creation | New connection every request | Uses pooled connections |
| Performance         | Slower                       | Faster                  |
| Connection Pooling  | No                           | Yes                     |
| Enterprise Usage    | Rare                         | Common                  |
| Spring Boot Usage   | Rare                         | Standard                |

---

# Connection Pooling

Connection Pooling is a technique where a set of database connections is created in advance and maintained in a pool.

Applications reuse these connections instead of creating a new one every time.

---

## Without Connection Pooling

```text
Request 1
      |
      v
Create Connection
      |
      v
Execute SQL
      |
      v
Close Connection

Request 2
      |
      v
Create Connection
      |
      v
Execute SQL
      |
      v
Close Connection

Request 3
      |
      v
Create Connection
      |
      v
Execute SQL
      |
      v
Close Connection
```

A new connection is created for every request.

---

## With Connection Pooling

```text
             Connection Pool

      +-----------------------+
      | Connection 1          |
      | Connection 2          |
      | Connection 3          |
      | Connection 4          |
      +-----------------------+

               ▲
               |
Application ----+
```

Application borrows a connection.

↓

Executes SQL.

↓

Returns the connection back to the pool.

No new connection is created.

---

## Advantages

- Faster response time
- Better scalability
- Reduced database load
- Reuses existing connections
- Recommended for enterprise applications

---

# Metadata

Metadata means **"Data about Data."**

JDBC provides two important metadata interfaces:

1. DatabaseMetaData
2. ResultSetMetaData

---

# 1. DatabaseMetaData

Provides information about:

- Database
- JDBC Driver
- Tables
- Schemas
- Supported SQL Features

Example:

```java
Connection con =
        DriverManager.getConnection(url, user, password);

DatabaseMetaData meta =
        con.getMetaData();
```

Common methods:

```
meta.getDatabaseProductName();

meta.getDatabaseProductVersion();

meta.getDriverName();

meta.getDriverVersion();

meta.getURL();

meta.getUserName();
```

Example Output:

```text
Database : MySQL

Version  : 8.4

Driver    : MySQL Connector/J

User      : root
```

---

# 2. ResultSetMetaData

Provides information about the columns in a ResultSet.

Example:

```java
ResultSet rs =
        st.executeQuery("SELECT * FROM employee");

ResultSetMetaData meta =
        rs.getMetaData();
```

Common methods:

```
meta.getColumnCount();

meta.getColumnName(1);

meta.getColumnLabel(1);

meta.getColumnTypeName(1);

meta.getColumnDisplaySize(1);

meta.isNullable(1);
```

Example:

```
System.out.println(meta.getColumnCount());

System.out.println(meta.getColumnName(1));

System.out.println(meta.getColumnTypeName(1));
```

Possible Output:

```text
Column Count : 4

Column Name  : id

Column Type  : INT
```

---

# DatabaseMetaData vs ResultSetMetaData

| Feature           | DatabaseMetaData         | ResultSetMetaData   |
|-------------------|--------------------------|---------------------|
| Information About | Database                 | ResultSet Columns   |
| Obtained From     | Connection               | ResultSet           |
| Interface Method  | getMetaData()            | getMetaData()       |
| Usage             | Database, Driver, Tables | Columns, Data Types |

---

# Summary

## Core JDBC Components

```text
DriverManager
        ↓
Connection
        ↓
Statement / PreparedStatement
        ↓
Execute SQL
        ↓
ResultSet
```

---

## Enterprise JDBC Flow

```text
Application
        |
        v
DataSource
        |
        v
Connection Pool
        |
        v
Connection
        |
        v
PreparedStatement
        |
        v
Database
        |
        v
ResultSet
```

---

# Key Takeaways

- `DriverManager` locates the correct JDBC Driver.
- `Connection` represents a session with the database.
- `Statement` executes static SQL.
- `PreparedStatement` executes parameterized SQL and prevents SQL Injection.
- `ResultSet` represents data returned from a SELECT query.
- Transactions group multiple SQL statements into one logical unit.
- Batch Processing improves performance for bulk operations.
- `SQLException` handles database-related errors.
- `DataSource` is the preferred way to obtain database connections.
- Connection Pooling improves performance by reusing existing connections.
- `DatabaseMetaData` provides information about the database.
- `ResultSetMetaData` provides information about query result columns.