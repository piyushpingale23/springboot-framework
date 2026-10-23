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

That's why, setter based and field based injection are partial injection.

Setter Injection is called partial injection because the object is created before all dependencies are injected, so it can temporarily exist in a partially initialized state.

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
