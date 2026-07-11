# Why Constructor Injection is Preferred Over Field Injection in Spring

## Introduction

Spring supports multiple types of Dependency Injection (DI):

1. Constructor Injection
2. Setter Injection
3. Field Injection

Although all three work, **Constructor Injection is the recommended approach in modern Spring applications**.

---

# 1. Field Injection Example

```java
@Component
public class Car {

    @Autowired
    private Engine engine;

    public void drive() {
        engine.start();
    }
}
```

### Internal Working

When Spring creates the bean:

```java
Car car = new Car();
```

At this point:

```
engine == null
```

Then Spring creates bean of Engine, but to private **engine**

Spring then uses **Reflection** to inject the dependency.

Conceptually:

```
Field field = Car.class.getDeclaredField("engine");
field.setAccessible(true);
field.set(car, engineBean);
```

The object is created first and dependencies are injected later.

---

# 2. Constructor Injection Example

```java
@Component
public class Car {

    private final Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        engine.start();
    }
}
```

### Internal Working

Spring first resolves the dependency:

```java
Engine engine = new Engine();
```

Then creates the bean:

```java
Car car = new Car(engine);
```

The object is fully initialized during creation.

---

# Why Constructor Injection is Preferred

## 1. Dependencies are Explicit

### Field Injection

```java
@Component
public class Car {

    @Autowired
    private Engine engine;
}
```

To understand the dependencies, you must inspect the fields.

---

### Constructor Injection

```java
public Car(Engine engine) {
    this.engine = engine;
}
```

The dependency is immediately visible.

Anyone reading the constructor knows:

```text
Car requires an Engine.
```

---

## 2. Mandatory Dependencies

### Constructor Injection

```java
public Car(Engine engine) {
    this.engine = engine;
}
```

A Car cannot be created without an Engine.

```
new Car(); // Compilation Error
```

The object is always valid.

---

### Field Injection

```java
@Autowired
private Engine engine;
```

The object can be created without its dependency.

```java
Car car = new Car();
```

For a short period:

```
engine == null
```

The object exists in an incomplete state.

---

## 3. Supports Final Fields

### Constructor Injection

```java
private final Engine engine;

public Car(Engine engine) {
    this.engine = engine;
}
```

The dependency can never be changed after creation.

This promotes immutability.

---

### Field Injection

```java
@Autowired
private final Engine engine;
```

Spring cannot inject into a final field after object creation.

Therefore, this approach is generally not used.

---

## 4. Easier Unit Testing

### Constructor Injection

```java
Engine mockEngine = new Engine();

Car car = new Car(mockEngine);
```

No Spring container is required.

Testing is simple.

---

### Field Injection

```java
Car car = new Car();
```

The dependency remains null.

You must either:

* Start Spring
* Use Reflection

Testing becomes more difficult.

---

## 5. No Hidden Dependencies

### Field Injection

```java
@Component
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SmsService smsService;
}
```

Dependencies are scattered throughout the class.

---

### Constructor Injection

```java
public OrderService(
        UserRepository userRepository,
        EmailService emailService,
        PaymentService paymentService,
        SmsService smsService) {
}
```

All dependencies are visible in one place.

---

## 6. Avoids Reflective Field Modification

### Field Injection

Spring uses Reflection:

```
Field field = Car.class.getDeclaredField("engine");
field.setAccessible(true);
field.set(car, engine);
```

Private fields are modified after object creation.

---

### Constructor Injection

```java
Car car = new Car(engine);
```

No reflective field modification is required.

Dependencies are supplied naturally through the constructor.

---

# Internal Lifecycle Comparison

## Constructor Injection

```text
Need Car Bean
        │
        ▼
Constructor Requires Engine
        │
        ▼
Create Engine Bean
        │
        ▼
Pass Engine to Constructor
        │
        ▼
Create Car Bean
        │
        ▼
Store Bean in Spring Container
```

---

## Field Injection

```text
Need Car Bean
        │
        ▼
Create Car Bean
        │
        ▼
Create Engine Bean
        │
        ▼
Locate @Autowired Field
        │
        ▼
Use Reflection
        │
        ▼
Inject Engine into Field
        │
        ▼
Store Bean in Spring Container
```

---

# Simple Analogy

## Constructor Injection

```text
Buying a car with an engine already installed.
```

The car is ready to drive immediately.

---

## Field Injection

```text
Buying a car first,
then someone installs the engine later.
```

The car exists before it is fully usable.

---
