# GraphQL Notes (Spring Boot)

## What is GraphQL?

GraphQL is a **query language for APIs** and a **runtime** that allows clients (Web, Mobile, etc.) to request **exactly the data they need** from the server.

- Developed by **Facebook (Meta)** in **2012**
- Open-sourced in **2015**
- Unlike REST APIs, where each endpoint returns a fixed response, GraphQL lets the **client decide which fields it wants**.

---

# REST vs GraphQL Example

## REST API

Suppose you want only the user's **name** and **email**.

### Request

```http
GET /users/1
```

### Response

```json
{
  "id": 1,
  "name": "Piyush",
  "email": "piyush@example.com",
  "phone": "9876543210",
  "address": {
    "city": "Pune",
    "country": "India"
  },
  "age": 25
}
```

> REST returns the complete resource, even if the client needs only a few fields.

---

## GraphQL API

### Request

```
query {
  user(id: 1) {
    name
    email
  }
}
```

### Response

```json
{
  "data": {
    "user": {
      "name": "Piyush",
      "email": "piyush@example.com"
    }
  }
}
```

> GraphQL returns only the requested fields.

---

# Why GraphQL?

GraphQL solves common REST API problems.

## 1. Over-fetching

The server returns more data than the client needs.

**Example**

Client needs only:

- Name
- Email

REST Response:

```json
{
  "id": 1,
  "name": "Piyush",
  "email": "piyush@example.com",
  "phone": "9876543210",
  "address": {
    "city": "Pune",
    "country": "India"
  },
  "age": 25
}
```

Extra fields like **phone**, **address**, and **age** are unnecessary.

---

## 2. Under-fetching

The server doesn't return all the required data, so the client must call multiple endpoints.

Example:

```
GET /users/1
GET /users/1/orders
GET /users/1/address
```

GraphQL can fetch everything in a single request.

---

# Main Components

## 1. Schema

A GraphQL Schema defines all available types, queries, mutations, and relationships.

Example:

```graphql
type User {
    id: ID
    name: String
    email: String
}
```

---

## 2. Query (Equivalent to REST GET)

Used to fetch data.

Example:

```
query {
    user(id: 1) {
        name
        email
    }
}
```

---

## 3. Mutation (Equivalent to REST POST, PUT, DELETE)

Used to create, update, or delete data.

Example:

```
mutation {
    createUser(name: "Piyush", email: "abc@gmail.com") {
        id
        name
    }
}
```

---

# GraphQL Request Flow

```text
             GraphQL Query
Client -------------------------> GraphQL Server
                                     |
                                     |
                                     V
                                 Resolver
                                     |
                                     |
                                     V
                    Database / REST API / Microservice
                                     |
                                     |
                                     V
                                Response
                                     |
                                     |
                                     V
                                  Client
```

---

# Resolver

A **Resolver** is a Java method that fetches the requested data.

Example in Spring Boot:

```java
@QueryMapping
public User fetchUserById(@Argument Long id) {
    return userService.findById(id);
}
```

GraphQL Query:

```
query {
    user(id: 1) {
        name
        email
    }
}
```

Spring Boot automatically invokes:

```java
fetchUserById();
```

The resolver fetches data from the Service layer, which retrieves it from the database.

---

# REST vs GraphQL Comparison

| Feature           | REST                           | GraphQL                        |
|-------------------|--------------------------------|--------------------------------|
| Endpoints         | Multiple (`/users`, `/orders`) | Single endpoint (`/graphql`)   |
| Data Returned     | Fixed                          | Client chooses required fields |
| Over-fetching     | Yes                            | No                             |
| Under-fetching    | Yes                            | No                             |
| Multiple Requests | Often required                 | Usually a single query         |
| Versioning        | Common (`v1`, `v2`)            | Usually unnecessary            |
| Strong Typing     | No standard schema             | Schema-driven                  |

---

# Spring Boot GraphQL Annotations

## 1. `@QueryMapping`

Used to handle GraphQL **Query** operations.

Equivalent to:

- REST `GET`

### GraphQL

```
query {
    getUserById(id: 1) {
        id
        name
    }
}
```

### Java

```java
@Controller
public class UserController {

    @QueryMapping
    public User getUserById(@Argument Long id) {
        return userService.findById(id);
    }

}
```

---

## 2. `@MutationMapping`

Used to handle:

- Create
- Update
- Delete

Equivalent to:

- REST POST
- REST PUT
- REST DELETE

### GraphQL

```
mutation {
    createUser(
        name: "Piyush",
        email: "abc@gmail.com"
    ) {
        id
        name
    }
}
```

### Java

```java
@MutationMapping
public User createUser(
        @Argument String name,
        @Argument String email) {

    return userService.createUser(name, email);
}
```

---

## 3. `@SchemaMapping`

Maps a Java method to a specific GraphQL **Type** and **Field**.

Useful for nested object resolution.

### Schema

```graphql
type User {
    id: ID
    name: String
    orders: [Order]
}
```

### Java

```java
@SchemaMapping(typeName = "User", field = "orders")
public List<Order> orders(User user) {
    return orderService.findByUserId(user.getId());
}
```

When the client requests:

```
query {
    user(id: 1) {
        name
        orders {
            id
            amount
        }
    }
}
```

Spring Boot automatically invokes the `orders()` resolver.

---

## 4. `@Argument`

Reads a single GraphQL argument.

### GraphQL

```
query {
    user(id: 1)
}
```

### Java

```java
@QueryMapping
public User user(@Argument Long id) {
    return service.findById(id);
}
```

---

## 5. `@Arguments`

Maps all GraphQL arguments into a Java DTO.

### GraphQL

```
mutation {
    createUser(
        input: {
            name: "Piyush"
            email: "abc@gmail.com"
        }
    ) {
        id
    }
}
```

### DTO

```java
public class UserInput {

    private String name;
    private String email;

}
```

### Java

```java
@MutationMapping
public User createUser(@Arguments UserInput input) {
    return service.create(input);
}
```

---

## 6. `@Controller`

Marks a class as a GraphQL Controller.

```java
@Controller
public class UserController {

}
```

> **Note:** Use `@Controller` instead of `@RestController` for GraphQL controllers.

---

# GraphQL Interview Questions & Answers (Spring Boot)

---

# Q1. What is a GraphQL Schema?

## Answer

A **GraphQL Schema** defines the structure of a GraphQL API. It specifies:

- Data types (e.g., `User`, `Order`)
- Queries (Read operations)
- Mutations (Create, Update, Delete operations)
- Subscriptions (Real-time operations)

### Example

```graphql
type User {
    id: ID
    name: String
}

type Order {
    id: ID
    amount: Float
}

type Query {
    user(id: ID!): User
    order(id: ID!): Order
}

type Mutation {
    createUser(name: String!): User
    createOrder(amount: Float!): Order
}
```

---

# Q2. What is the difference between `type User` and `type Query`?

## Answer

### `type User`

- Defines the structure of a `User` object.
- Represents a data model.
- Cannot be executed directly.

Example:

```graphql
type User {
    id: ID
    name: String
}
```

### `type Query`

- Defines read operations.
- These operations can be executed by the client.

Example:

```graphql
type Query {
    user(id: ID!): User
}
```

---

# Q3. What is `type Mutation`?

## Answer

A **Mutation** defines write operations, such as:

- Create
- Update
- Delete

Example:

```graphql
type Mutation {
    createUser(name: String!): User
}
```

---

# Q4. How does Spring Boot map GraphQL Query to Java methods?

## Answer

Spring Boot reads the GraphQL schema and searches for Java methods annotated with:

- `@QueryMapping`
- `@MutationMapping`
- `@SchemaMapping`

### Schema

```graphql
type Query {
    user(id: ID!): User
}
```

### Java

```java
@Controller
public class UserController {

    @QueryMapping
    public User user(@Argument Long id) {
        return service.findById(id);
    }

}
```

When the client executes:

```
query {
    user(id: 1) {
        id
        name
    }
}
```

Spring automatically invokes:

```java
user();
```

---

# Q5. Does Spring Boot map GraphQL fields based on the Controller class name?

## Answer

**No.**

Spring does **not** care whether the method is inside:

- `UserController`
- `OrderController`
- `ProductController`

It only matches the **GraphQL field name**.

Example:

```graphql
type Query {
    user(id: ID!): User
}
```

Spring searches for:

```java
@QueryMapping
public User user();
```

> **The controller class name is irrelevant.**

---

# Q6. How does Spring Boot match GraphQL fields to Java methods?

## Answer

By default:

```text
GraphQL Field Name
        │
        ▼
Java Method Name
```

Example:

### Schema

```graphql
type Query {
    user(id: ID!): User
}
```

### Java

```java
@QueryMapping
public User user() {
}
```

Since both names are `user`, Spring maps them automatically.

---

# Q7. What if the Java method name is different?

## Answer

Use the `name` attribute.

Example:

```java
@QueryMapping(name = "user")
public User getUser(@Argument Long id) {
}
```

Now the GraphQL field:

```
user
```

maps to:

```java
getUser();
```

---

# Q8. Can a project have multiple `.graphqls` files?

## Answer

**Yes.**

Spring Boot automatically reads all `.graphqls` files and merges them into a single GraphQL schema.

Example:

```text
resources/
└── graphql/
    ├── user.graphqls
    ├── order.graphqls
    └── product.graphqls
```

All files are merged during application startup.

---

# Q9. Does Spring Boot search for Java methods in every `.graphqls` file?

## Answer

**No.**

Spring Boot follows this process:

```text
Step 1
Read all .graphqls files
        │
        ▼
Step 2
Merge them into one GraphQL schema
        │
        ▼
Step 3
Scan all @Controller classes
        │
        ▼
Step 4
Find @QueryMapping, @MutationMapping, @SchemaMapping methods
        │
        ▼
Step 5
Match GraphQL field names with Java methods
```

> Spring searches **controller methods**, not schema files.

---

# Q10. Can two `.graphqls` files define different Queries?

## Answer

**Yes.**

### user.graphqls

```graphql
extend type Query {
    user(id: ID!): User
}
```

### order.graphqls

```graphql
extend type Query {
    order(id: ID!): Order
}
```

Spring merges them into:

```graphql
type Query {
    user(id: ID!): User
    order(id: ID!): Order
}
```

---

# Q11. Can two `.graphqls` files define the same Query?

## Answer

**No.**

### user.graphqls

```graphql
extend type Query {
    user(id: ID!): User
}
```

### another.graphqls

```graphql
extend type Query {
    user(id: ID!): User
}
```

This creates a duplicate field.

Spring Boot throws an error during startup because the GraphQL field `user` is defined more than once.

---

# Q12. Can two controller methods handle the same GraphQL field?

## Answer

**No.**

Example:

```java
@Controller
class UserController {
    @QueryMapping
    public User user() {
    }
}
```

```java
@Controller
class AnotherController {

    @QueryMapping
    public User user() {
    }
}
```

Both methods map to the same GraphQL field:

```
user
```

Spring Boot throws an **Ambiguous Mapping** error.

---

# Q13. Can one `.graphqls` file contain multiple types?

## Answer

**Yes.**

Example:

```graphql
type User {
    id: ID
}

type Order {
    id: ID
}

type Product {
    id: ID
}
```

There is **no restriction** on the number of types in a single file.

---

# Q14. Can one `.graphqls` file contain multiple Queries?

## Answer

**Yes.**

Example:

```
type Query {

    user(id: ID!): User
    users: [User]

    order(id: ID!): Order
    orders: [Order]

    product(id: ID!): Product

}
```

---

# Q15. Can one `.graphqls` file contain multiple Mutations?

## Answer

**Yes.**

Example:

```graphql
type Mutation {

    createUser(name: String!): User

    updateUser(id: ID!, name: String!): User

    deleteUser(id: ID!): Boolean

    createOrder(amount: Float!): Order

}
```

---

# Q16. Is it mandatory to keep one type per `.graphqls` file?

## Answer

**No.**

GraphQL does **not** impose this restriction.

You can choose either approach:

- One file for the entire schema.
- Multiple files organized by domain.

Both approaches are valid.

---

# Q17. What is the recommended project structure for GraphQL schemas?

## Answer

The recommended approach is to organize schemas by **domain**.

```text
resources/
└── graphql/
    ├── user.graphqls
    ├── order.graphqls
    └── product.graphqls
```

Each file typically contains:

- Entity type
- Related Queries
- Related Mutations

Example:

### user.graphqls

```graphql
type User {
    id: ID
    name: String
}

extend type Query {
    user(id: ID!): User
    users: [User]
}

extend type Mutation {
    createUser(name: String!): User
}
```

This structure improves:

- Readability
- Maintainability
- Scalability

---

# Q18. What is the complete execution flow of a GraphQL request in Spring Boot?

## Answer

```text
Client
   │
   ▼
GraphQL Query
   │
   ▼
Spring Boot reads the merged GraphQL Schema
   │
   ▼
Finds the requested field (e.g., user)
   │
   ▼
Scans all @Controller classes
   │
   ▼
Finds the matching @QueryMapping/@MutationMapping/@SchemaMapping method
   │
   ▼
Invokes the Service layer
   │
   ▼
Invokes the Repository layer
   │
   ▼
Fetches data from the Database
   │
   ▼
Returns a Java Object
   │
   ▼
Spring converts the Java Object into a GraphQL Response
   │
   ▼
Client receives the JSON response
```

---

# Key Takeaways

- A **Schema** defines the GraphQL API structure.
- **Queries** are used to read data.
- **Mutations** are used to create, update, or delete data.
- Spring Boot maps GraphQL fields to Java methods using `@QueryMapping`, `@MutationMapping`, and `@SchemaMapping`.
- Method names should match GraphQL field names, or you can use the `name` attribute.
- Multiple `.graphqls` files are automatically merged into a single schema.
- Organizing schemas by domain (e.g., `user.graphqls`, `order.graphqls`) is considered a best practice.
- The complete request flow is:
  **Client → GraphQL Query → Schema → Controller → Service → Repository → Database → Response → Client**

---

# GraphQL Interview Questions & Answers (Part 2)

---

# Q19. How does a user execute (hit) GraphQL queries?

## Answer

Unlike REST APIs, where each operation has a different URL, GraphQL typically exposes a **single endpoint**.

Example:

```http
POST http://localhost:8080/graphql
```

The client sends the **GraphQL query** in the request body.

---

## Example 1: Fetch a User

### GraphQL Schema

```graphql
type Query {
    user(id: ID!): User
}
```

### HTTP Request

```http
POST /graphql
Content-Type: application/json
```

### Request Body

```json
{
  "query": "query { user(id: 1) { id name } }"
}
```

---

## Example 2: Create a User

### GraphQL Schema

```graphql
type Mutation {
    createUser(name: String!): User
}
```

### HTTP Request

```http
POST /graphql
Content-Type: application/json
```

### Request Body

```json
{
  "query": "mutation { createUser(name: \"Piyush\") { id name } }"
}
```

---

# Q20. How can a user execute GraphQL queries?

## Answer

A user can execute GraphQL queries using various GraphQL clients or frontend applications.

### Common GraphQL Clients

- GraphiQL (Browser UI)
- Altair GraphQL Client
- Postman
- Insomnia
- Apollo Client (React/Angular)
- curl
- Any frontend application (Angular, React, Vue, etc.)

---

## Example using Postman

### URL

```http
POST http://localhost:8080/graphql
```

### Headers

```text
Content-Type: application/json
```

### Body (Raw JSON)

```json
{
  "query": "query { users { id name } }"
}
```

---

## Example using cURL

```bash
curl -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-d '{
  "query":"query { users { id name } }"
}'
```

---

# Q21. What happens internally when a user executes a GraphQL query?

## Answer

Suppose the client sends the following GraphQL query:

```
query {
    user(id: 1) {
        id
        name
    }
}
```

The execution flow is:

```text
Client (Postman / Angular / React)

        │
        ▼

POST /graphql

        │
        ▼

Spring GraphQL receives the request

        │
        ▼

Reads the merged GraphQL Schema

        │
        ▼

Looks for the Query field:

user(id: ID!): User

        │
        ▼

Scans all @Controller classes

        │
        ▼

Finds:

@QueryMapping
public User user(@Argument Long id)

        │
        ▼

Calls the Service Layer

        │
        ▼

Calls the Repository Layer

        │
        ▼

Fetches data from the Database

        │
        ▼

Returns a User object

        │
        ▼

Spring GraphQL converts the Java object into a GraphQL JSON response

        │
        ▼

Response sent back to the client
```

---

# Q22. Does GraphQL use different URLs like REST APIs?

## Answer

**No.**

### REST API

Each operation usually has its own endpoint.

```http
GET    /users
GET    /users/1
POST   /users
PUT    /users/1
DELETE /users/1
```

---

### GraphQL

GraphQL typically exposes **one endpoint**.

```http
POST /graphql
```

The operation (**Query**, **Mutation**, or **Subscription**) is specified in the **request body**, not in the URL.

Example:

```json
{
  "query": "query { users { id name } }"
}
```

or

```json
{
  "query": "mutation { createUser(name:\"Piyush\") { id name } }"
}
```

---

# Q23. How does Spring Boot know which Java method to execute?

## Answer

Spring GraphQL examines the **field name** in the incoming GraphQL query and matches it to a controller method annotated with `@QueryMapping` or `@MutationMapping`.

---

### GraphQL Request

```
query {
    user(id: 1) {
        id
        name
    }
}
```

---

### GraphQL Schema

```graphql
type Query {
    user(id: ID!): User
}
```

---

### Controller

```java
@Controller
public class UserController {

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.findById(id);
    }

}
```

The GraphQL field:

```
user
```

matches the Java method:

```java
user();
```

Therefore, Spring automatically invokes:

```java
user(@Argument Long id);
```

---

## What if the Java method name is different?

Use the `name` attribute of `@QueryMapping`.

Example:

```java
@QueryMapping(name = "user")
public User getUser(@Argument Long id) {
    return userService.findById(id);
}
```

Here:

- GraphQL field → `user`
- Java method → `getUser()`

Spring maps them correctly using the `name` attribute.

---

# Key Takeaways

- GraphQL usually exposes a **single endpoint** (`/graphql`).
- Clients send GraphQL operations in the **request body**.
- GraphQL queries can be executed using GraphiQL, Altair, Postman, Insomnia, Apollo Client, cURL, or frontend applications.
- Spring GraphQL reads the merged schema, identifies the requested field, and invokes the corresponding `@QueryMapping` or `@MutationMapping` method.
- By default, the **GraphQL field name** must match the **Java method name**, unless explicitly mapped using the `name` attribute.

---