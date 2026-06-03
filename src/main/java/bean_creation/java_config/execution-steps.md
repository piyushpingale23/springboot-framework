# Spring Java-Based Configuration with `@Bean`

# Complete Internal Flow (Step-by-Step)

---

# Example

## Car.java

```java
package bean_creation.java_config.constructor;

public class Car {

    private String model;
    private String type;

    public Car(String model, String type) {
        this.model = model;
        this.type = type;

        System.out.println("Car Constructor Called");
    }

    public void startCar() {
        System.out.println("Car Started : " + model + " : " + type);
    }
}
```

---

## SpringConfig.java

```java
package bean_creation.java_config.constructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public Car car() {

        return new Car("BMW", "Electric");
    }
}
```

---

## Main.java

```java
package bean_creation.java_config.constructor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {

        ApplicationContext context =
                new AnnotationConfigApplicationContext(SpringBeanConfigConstructor.class);

        Car car = context.getBean("car", Car.class);

        car.startCar();
    }
}
```

---

# MAIN QUESTION

# How does Spring internally work step-by-step?

---

# STEP 1

# JVM Starts Main Method

Execution starts from:

```java
public static void main(String[] args) {
    
}
```

---

# STEP 2

# Spring Container Object Creation

```java
ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
```

This line creates Spring IoC Container.

---

# Visualization

```text
Main Method
    |
    |
    ---> AnnotationConfigApplicationContext
                    |
                    ---> Spring IoC Container Created
```

---

# STEP 3

# Spring Reads Configuration Class

Spring receives:

```
SpringConfig.class
```

Spring checks:

```
@Configuration
```

annotation.

---

# Why Important?

Because Spring now understands:

```text
"This class contains bean definitions"
```

---

# Visualization

```text
SpringConfig.class
        |
        |
        ---> @Configuration found
                    |
                    ---> Treat class as Configuration Class
```

---

# STEP 4

# Spring Creates Proxy Object (CGLIB)

Spring does NOT use original class directly.

It creates:

```text
SpringConfig$$EnhancerBySpringCGLIB
```

proxy subclass.

---

# Why Proxy Needed?

To maintain:

* Singleton behavior
* Bean lifecycle
* Dependency handling
* Interception of @Bean methods

---

# Visualization

```text
Original Class
      SpringConfig
            |
            |
            ---> Spring Creates Proxy
                        |
                        ---> SpringConfig$$CGLIB
```

---

# STEP 5

# Spring Scans Methods

Spring scans all methods inside configuration class.

```
@Bean
public Car car()
```

Spring detects:

```
@Bean
```

---

# Meaning of @Bean

```text
"The returned object of this method should become Spring Bean"
```

---

# Visualization

```text
SpringConfig
      |
      |----> car()
                |
                ---> @Bean Found
```

---

# STEP 6

# Spring Executes @Bean Method

Spring internally calls:

```
car()
```

method.

---

# Actual Execution

```
return new Car("BMW", "Electric");
```

Now JVM executes constructor:

```
Car(String model, String type)
```

---

# Visualization

```text
Spring Container
      |
      ---> Execute car()
                    |
                    ---> new Car(...)
                                |
                                ---> Constructor Called
```

---

# STEP 7

# Bean Object Created

Now object is created in memory.

Example:

```text
Car@4ab34cd
```

---

# STEP 8

# Spring Stores Bean in IoC Container

Spring stores object internally.

---

# Internal Structure (Conceptual)

```text
IoC Container
---------------------------------
Bean Name        Object
---------------------------------
car              Car@4ab34cd
---------------------------------
```

---

# Default Bean Name

Bean name becomes:

```text
Method Name
```

So:

```
@Bean
public Car car()
```

creates bean id:

```text
car
```

---

# STEP 9

# getBean() Called

```java
Car car = context.getBean("car", Car.class);
```

Spring searches container:

```text
Find Bean with id = car
```

---

# Visualization

```text
getBean("car")
      |
      |
      ---> IoC Container
                  |
                  ---> Bean Found
                              |
                              ---> Return Existing Object
```

---

# STEP 10

# Bean Returned to Application

Spring returns SAME singleton object.

```
Car@4ab34cd
```

---

# STEP 11

# Method Executes

```
car.startCar();
```

Output:

```text
Car Started : BMW : Electric
```

---

# COMPLETE FLOW VISUALIZATION

```text
Main Method
    |
    ---> AnnotationConfigApplicationContext
                |
                ---> Read SpringConfig.class
                            |
                            ---> @Configuration Found
                                        |
                                        ---> Create CGLIB Proxy
                                                    |
                                                    ---> Scan @Bean methods
                                                                |
                                                                ---> Execute car()
                                                                            |
                                                                            ---> new Car()
                                                                                        |
                                                                                        ---> Constructor Called
                                                                                                    |
                                                                                                    ---> Object Created
                                                                                                                |
                                                                                                                ---> Store inside IoC Container
                                                                                                                            |
                                                                                                                            ---> getBean("car")
                                                                                                                                        |
                                                                                                                                        ---> Return Singleton Object
```

---

# WHEN EXACTLY IS BEAN CREATED?

Bean created during:

```
new AnnotationConfigApplicationContext(SpringConfig.class)
```

NOT during:

```
getBean()
```

because singleton beans are eagerly initialized by default.

---

# Important Concept

# Singleton Bean Lifecycle

```text
Container Start
      |
      ---> Bean Created
                |
                ---> Bean Stored
                          |
                          ---> Bean Reused
```

---

# Prototype Bean Lifecycle

If:

```
@Bean
@Scope("prototype")
```

then:

```text
Container Start
      |
      ---> NO Object Created
                    |
                    ---> getBean()
                                |
                                ---> NEW Object Created Every Time
```

---

# WHERE SHOULD WE USE @Bean?

Use `@Bean` inside:

```
@Configuration
```

class.

---

# Recommended Structure

```java
@Configuration
public class AppConfig {

    @Bean
    public Car car() {
        return new Car();
    }
}
```

---

# Why Not Normal Class?

Because:

```
@Configuration
```

enables:

* CGLIB proxy
* Singleton management
* Proper dependency handling
* Bean interception

---

# INTERNAL DIFFERENCE

# @Configuration

```text
FULL MODE
```

Spring creates proxy.

---

# @Component

```text
LITE MODE
```

No proxy.

Direct Java method calls happen.

Can accidentally create multiple objects.

---

# BEST PRACTICE

Always use:

```
@Configuration
```

for configuration classes containing `@Bean`.

---

# REAL-TIME ANALOGY

## Think of Spring Container Like Hotel Reception

```text
Customer asks for Room
        |
        ---> Reception checks registry
                    |
                    ---> If room already exists
                                |
                                ---> Return existing room
```

Singleton Bean = Same room reused.

Prototype Bean = New room every request.

---

# FINAL SUMMARY

| Step | What Happens                   |
|------|--------------------------------|
| 1    | Main method starts             |
| 2    | IoC container created          |
| 3    | Spring reads `@Configuration`  |
| 4    | CGLIB proxy created            |
| 5    | Spring scans `@Bean` methods   |
| 6    | Spring executes method         |
| 7    | Object created                 |
| 8    | Bean stored in container       |
| 9    | `getBean()` searches container |
| 10   | Bean returned                  |
| 11   | Application uses bean          |

---

