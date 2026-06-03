# Spring MVC Request & Response Annotations

## 1. @RequestMapping

### Purpose

Used to map an HTTP request URL to a controller class or handler method.

Can be used at both class level and method level.

### Class-Level Example

```
@RequestMapping("/users")
```

### Method-Level Example

```
@RequestMapping("/welcome")
```

### Final URL

```text
Class Level  : /users
Method Level : /welcome

Final URL    : /users/welcome
```

### HTTP Method Restriction

```
@RequestMapping(value = "/welcome",method = RequestMethod.GET)
```

### Preferred Modern Alternative

```
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@PatchMapping
```

---

## 2. @PathVariable

### Purpose

Used to bind a value from the URL path to a method parameter.

### Example

```java
@GetMapping("/users/{id}")
public String getUser(@PathVariable Long id);
```

Request:

```text
GET /annotations/users/101
```

Value Extracted:

```text
id = 101
```

### Common REST Usage

```text
GET    /users/{id}
PUT    /users/{id}
DELETE /users/{id}
GET    /products/{id}
```

### Explicit Name Mapping

```java
@PathVariable("id") Long userId;
```

### Shorthand

```java
@PathVariable Long id;
```

Used when parameter name and path variable name are the same.

---

## 3. @RequestParam

### Purpose

Used to bind a query parameter from the URL to a method parameter.

### Example

```java
@GetMapping("/search")
public String searchUser(@RequestParam String name) {
    return name;
}
```

Request:

```text
GET /annotations/search?name=Jack
```

Value Extracted:

```text
name = Jack
```

### Multiple Request Parameters

```java
@GetMapping("/employees")
public String getEmployee(@RequestParam Long id, @RequestParam String department) {
    return "...";
}
```

Request:

```text
GET /annotations/employees?id=101&department=IT
```

Values Extracted:

```text
id = 101
department = IT
```

### Explicit Parameter Name

```java
@RequestParam("name") String employeeName;
```

Request:

```text
GET /employees?name=John
```

### Optional Parameter

```java
@RequestParam(required = false)
String department;
```

### Default Value

```java
@RequestParam(defaultValue = "IT")
String department;
```

If the parameter is not provided, Spring uses the default value.

---

## Difference Between @PathVariable and @RequestParam

### @PathVariable

Value comes from the URL path.

Example:

```text
GET /users/101
```

```java
@PathVariable Long id;
```

---

### @RequestParam

Value comes from the query string.

Example:

```text
GET /users?id=101
```

```java
@RequestParam Long id;
```

---

### When to Use Which?

Use `@PathVariable` when identifying a specific resource.

```text
/users/101
/products/10
/orders/500
```

Use `@RequestParam` for filtering, searching, sorting, pagination, etc.

```text
/users?page=1
/users?sort=name
/users?department=IT
/users?name=John
```

---

## @RequestBody

### Purpose

Used to bind the HTTP request body to a Java object.

Spring automatically converts the incoming JSON data into a Java object using Jackson.

### Example

```java
@PostMapping("/users")
public String createUser(
        @RequestBody User user) {

    return "User Created: " + user.getName();
}
```

### Request

```http
POST /annotations/users
Content-Type: application/json
```

Request Body:

```json
{
  "id": 101,
  "name": "Jack"
}
```

### Object Created By Spring

```java
User {
    id = 101,
    name = "Jack";
}
```

### Response

```text
User Created: Jack
```

### How It Works Internally

1. Client sends JSON in the request body.
2. Spring receives the HTTP request.
3. Jackson deserializes JSON into a Java object.
4. Spring passes the populated object to the controller method.
5. The controller uses the object like any normal Java object.

### Serialization vs Deserialization

#### Deserialization

JSON → Java Object

```json
{
  "id": 101,
  "name": "Jack"
}
```

↓

```java
User user;
```

This happens while processing the request.

#### Serialization

Java Object → JSON

```java
User user;
```

↓

```json
{
  "id": 101,
  "name": "Jack"
}
```

This happens while sending the response.

---

## @ModelAttribute

### Purpose

Used to bind request parameters (form data, query parameters) directly to a Java object.

Spring creates the object and populates its fields using matching request parameter names.

### Example

```java
@PostMapping("/employees")
public String createEmployee(
        @ModelAttribute User user) {

    return "Employee Created: " + user.getName();
}
```

### Request

```http
POST /annotations/employees
Content-Type: application/x-www-form-urlencoded
```

Request Parameters:

```text
id=101
name=Jack
```

### Object Created By Spring

```java
User {
    id = 101,
    name = "Jack";
}
```

### Response

```text
Employee Created: Jack
```

### How It Works Internally

1. Client sends form fields or request parameters.
2. Spring creates the target object.
3. Spring matches parameter names to object fields.
4. Spring calls setter methods.
5. The populated object is passed to the controller method.

Example:

```text
id=101
name=Jack
```

↓

```
user.setId(101L);
user.setName("Piyush");
```

↓

```java
User user;
```

### Common Usage

HTML Form Submission:

```html
<form method="post">
    <input name="id">
    <input name="name">
</form>
```

Controller:

```java
@PostMapping("/employees")
public String createEmployee(@ModelAttribute User user){}
```

### Explicit Model Name

```java
@ModelAttribute("employee")
User user;
```

Usually the model name is omitted.

---

## Difference Between @RequestBody and @ModelAttribute

### @RequestBody

Reads data from the HTTP request body.

Request:

```http
POST /users
Content-Type: application/json
```

```json
{
  "id": 101,
  "name": "Jack"
}
```

Controller:

```java
@RequestBody User user;
```

Uses:

```text
JSON → Java Object
```

Typically handled through Jackson.

---

### @ModelAttribute

Reads data from request parameters or form fields.

Request:

```http
POST /employees
Content-Type: application/x-www-form-urlencoded
```

```text
id=101
name=Jack
```

Controller:

```java
@ModelAttribute User user;
```

Uses:

```text
Form Fields / Query Parameters → Java Object
```

No JSON required.

---

## When to Use Which?

### Use @RequestBody

For REST APIs.

```http
POST /users
Content-Type: application/json
```

```json
{
  "id": 101,
  "name": "Jack"
}
```

### Use @ModelAttribute

For HTML forms and form submissions.

```http
POST /employees
Content-Type: application/x-www-form-urlencoded
```

```text
id=101
name=Jack
```

### Interview Answer

```text
@RequestBody:
- Reads data from request body.
- Mostly used in REST APIs.
- Commonly works with JSON.
- Uses Jackson for deserialization.

@ModelAttribute:
- Reads data from request parameters/form fields.
- Commonly used with HTML forms.
- Binds request parameters directly to a Java object.
```
