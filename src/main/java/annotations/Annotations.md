# Spring & Spring Boot Important Annotations

## 1. Spring IoC Container

### What is a Bean?

A Bean is an object whose lifecycle is managed by the Spring IoC Container.

Spring is responsible for:

* Creating beans
* Injecting dependencies
* Managing bean lifecycle
* Destroying beans

---

## 2. Core Spring Framework Annotations

### Component Scanning & Bean Definition

| Annotation        | Description                                                                  |
|-------------------|------------------------------------------------------------------------------|
| `@Component`      | Generic Spring-managed bean.                                                 |
| `@Controller`     | Spring MVC Controller.                                                       |
| `@RestController` | Combines `@Controller` + `@ResponseBody`. Used for REST APIs.                |
| `@Service`        | Service layer bean containing business logic.                                |
| `@Repository`     | DAO/Persistence layer bean. Also translates database exceptions.             |
| `@Configuration`  | Class containing bean definitions.                                           |
| `@Bean`           | Method-level annotation that registers the returned object as a Spring bean. |

### Notes

#### @Component

```java
@Component
public class EmailService {
}
```

Detected automatically through component scanning.

---

#### @Configuration

```java
@Configuration
public class AppConfig {

    @Bean
    public EmailService emailService() {
        return new EmailService();
    }
}
```

Used when you want to manually create beans.

---

#### @Bean

```java
@Bean
public EmailService emailService() {
    return new EmailService();
}
```

Registers the returned object as a bean.

---

## 3. Dependency Injection Annotations

| Annotation   | Description                               |
|--------------|-------------------------------------------|
| `@Autowired` | Inject bean by type.                      |
| `@Qualifier` | Select a specific bean.                   |
| `@Primary`   | Default bean when multiple beans exist.   |
| `@Value`     | Inject literal values or property values. |
| `@Resource`  | Inject bean by name.                      |

### Example

```java
@Autowired
@Qualifier("emailService")
private NotificationService service;
```

---

### Bean Ambiguity Resolution

When multiple beans of the same type exist:

```text
NoUniqueBeanDefinitionException
```

Solutions:

#### @Qualifier

```java
@Autowired
@Qualifier("emailService")
private NotificationService service;
```

#### @Primary

```java
@Bean
@Primary
public NotificationService emailService() {
    return new EmailService();
}
```

#### @Resource

```java
@Resource(name = "emailService")
private NotificationService service;
```

#### @Profile

```java
@Bean
@Profile("dev")
public NotificationService notificationService() {
    return new MockNotificationService();
}
```

---

## 4. Bean Lifecycle Annotations

| Annotation       | Description                                          |
|------------------|------------------------------------------------------|
| `@PostConstruct` | Called after bean creation and dependency injection. |
| `@PreDestroy`    | Called before bean destruction.                      |

Example:

```java
@PostConstruct
public void init() {
    System.out.println("Bean Initialized");
}

@PreDestroy
public void destroy() {
    System.out.println("Bean Destroyed");
}
```

---

## 5. Bean Scope Annotations

| Scope       | Description                      |
|-------------|----------------------------------|
| singleton   | Default. One bean per container. |
| prototype   | New bean every request.          |
| request     | One bean per HTTP request.       |
| session     | One bean per HTTP session.       |
| application | One bean per ServletContext.     |

Example:

```java
@Component
@Scope("prototype")
public class MyBean {
}
```

---

## 6. Lazy Initialization

```java
@Lazy
@Component
public class EmailService {
}
```

Bean is created only when first requested.

Default Spring behavior:

```text
Singleton beans -> Eager Initialization
```

---

## 7. Spring MVC Annotations

### Request Mapping

| Annotation        | Description              |
|-------------------|--------------------------|
| `@RequestMapping` | Generic request mapping. |
| `@GetMapping`     | Handles HTTP GET.        |
| `@PostMapping`    | Handles HTTP POST.       |
| `@PutMapping`     | Handles HTTP PUT.        |
| `@DeleteMapping`  | Handles HTTP DELETE.     |
| `@PatchMapping`   | Handles HTTP PATCH.      |

Example:

```java
@GetMapping("/users")
public List<User> getUsers() {
    return users;
}
```

---

### Request/Response Binding

| Annotation        | Description                 |
|-------------------|-----------------------------|
| `@RequestBody`    | Request JSON → Java Object  |
| `@ResponseBody`   | Java Object → Response Body |
| `@ModelAttribute` | Form data binding           |
| `@PathVariable`   | Extract value from URL path |
| `@RequestParam`   | Extract query parameter     |
| `@RequestHeader`  | Extract HTTP header         |

Example:

```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return service.find(id);
}
```

---

## 8. Exception Handling

### @ExceptionHandler

```java
@ExceptionHandler(UserNotFoundException.class)
public String handleException() {
    return "User Not Found";
}
```

---

### @ControllerAdvice

Global exception handling:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
}
```

---

## 9. Spring Boot Annotations

| Annotation                       | Description                                                                  |
|----------------------------------|------------------------------------------------------------------------------|
| `@SpringBootApplication`         | Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. |
| `@EnableAutoConfiguration`       | Automatically configures beans based on dependencies.                        |
| `@ComponentScan`                 | Scans packages for Spring components.                                        |
| `@PropertySource`                | Loads custom properties files.                                               |
| `@ConfigurationProperties`       | Binds properties to a POJO.                                                  |
| `@EnableConfigurationProperties` | Registers ConfigurationProperties beans.                                     |
| `@ConfigurationPropertiesScan`   | Automatically scans ConfigurationProperties classes.                         |
| `@Profile`                       | Creates beans only for specific environments.                                |
| `@ConditionalOnProperty`         | Conditional bean creation.                                                   |
| `@ConditionalOnMissingBean`      | Create bean only if no matching bean exists.                                 |

---

## 10. @SpringBootApplication Breakdown

```
@SpringBootApplication
```

Equivalent to:

```
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

---

### @SpringBootConfiguration

Equivalent to:

```
@Configuration
```

Marks the class as a configuration source.

---

### @EnableAutoConfiguration

Purpose:

```text
Automatically configure infrastructure beans
based on dependencies present in classpath.
```

Examples:

* Tomcat
* DispatcherServlet
* Jackson
* DataSource
* JPA

---

### @ComponentScan

Scans:

```text
Package of main class
+
All sub-packages
```

for:

```text
@Component
@Service
@Repository
@Controller
@RestController
```

---

## Startup Flow

```java
@SpringBootApplication
public class MyApplication {
}
```

### Step 1

Load configuration class.

### Step 2

Auto Configuration

Spring checks dependencies and creates infrastructure beans.

### Step 3

Component Scanning

Registers application beans.

---

## 11. Property Management

### @PropertySource

Purpose:

Load custom property files.

```java
@Configuration
@PropertySource("classpath:custom.properties")
public class AppConfig {
}
```

### Important Notes

Spring Boot automatically loads:

```text
application.properties
application.yml
application-dev.properties
application-prod.properties
```

Custom files are NOT loaded automatically.

---

### classpath:

```
@PropertySource("classpath:custom.properties")
```

Refers to:

```text
src/main/resources/custom.properties
```

Resources are copied into the application's classpath during build.

---

## 12. @ConfigurationProperties

Purpose:

Bind a group of properties to a Java object.

Example:

```properties
app.name=My Application
app.version=1.0
app.owner=Piyush
```

```java
@ConfigurationProperties(prefix="app")
public class AppProperties {

    private String name;
    private String version;
    private String owner;

    // getters/setters
}
```

---

## 13. @EnableConfigurationProperties

Registers ConfigurationProperties classes as Spring beans.

```
@EnableConfigurationProperties(AppProperties.class)
```

---

## 14. Conditional Bean Creation

### @ConditionalOnProperty

```java
@Bean
@ConditionalOnProperty(
    name="feature.email.enabled",
    havingValue="true"
)
public EmailService emailService() {
    return new EmailService();
}
```

Bean created only when:

```properties
feature.email.enabled=true
```

---

### @ConditionalOnMissingBean

```java
@Bean
@ConditionalOnMissingBean
public EmailService emailService() {
    return new EmailService();
}
```

Creates bean only if no bean of that type already exists.

---

## 15. Testing Annotations

| Annotation           | Description                         |
|----------------------|-------------------------------------|
| `@SpringBootTest`    | Loads complete application context. |
| `@TestConfiguration` | Test-specific configuration.        |
| `@MockBean`          | Replaces bean with Mockito mock.    |
| `@WebMvcTest`        | Tests only MVC layer.               |

---

## Recommended Dependency Injection

Prefer Constructor Injection:

```java
@Service
public class UserService {

    private final EmailService emailService;

    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

Advantages:

* Immutable dependencies
* Easier unit testing
* No reflection-based field injection
* Recommended by Spring Team

---

# Quick Revision

## Bean Creation

```text
@Component → Generic Spring annotation used to mark a class as a Spring-managed bean (base stereotype).
@Service → Specialization of @Component used for service layer classes containing business logic.
@Repository → Specialization of @Component used for DAO layer classes and also provides exception translation for database errors.
@Controller → Specialization of @Component used in Spring MVC to handle web requests and return views.
@RestController → Combination of @Controller + @ResponseBody, used for REST APIs to return JSON/XML responses directly.
@Bean
```

## Dependency Injection

```text
@Autowired → Injects a dependency into a field/constructor/method by type from the Spring container.
@Qualifier → Specifies which bean to inject when multiple beans of the same type exist.
@Primary → Marks a bean as the default choice when multiple beans of the same type exist.
@Resource → Injects a bean by name (not type) when multiple beans exist.
@Value → Injects values from .properties / .yml files or literals into fields.
```

## Configuration

```text
@Configuration → Indicates that a class declares one or more Spring bean definitions; it replaces XML configuration and is used to define beans using @Bean methods.

@PropertySource → Used to load external property files (like .properties or .yml) into Spring’s Environment so values can be accessed using @Value or Environment.

@ConfigurationProperties → Binds external configuration properties (from application.properties or application.yml) to a Java POJO in a type-safe way using a prefix.

@EnableConfigurationProperties → Enables support for classes annotated with @ConfigurationProperties, making them eligible for binding and registration as Spring beans when not using component scanning.
```

## Spring Boot

```text
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
```

## MVC

```text
@RequestMapping → Base annotation used to map HTTP requests to handler methods or classes; supports all HTTP methods (GET, POST, PUT, DELETE, etc.) and can be used at class or method level.

@GetMapping → Specialized version of @RequestMapping for handling HTTP GET requests; used to retrieve data from the server.

@PostMapping → Specialized version of @RequestMapping for handling HTTP POST requests; used to send data to the server (create resources).

@RequestBody → Binds the HTTP request body (usually JSON/XML) to a Java object; used for reading incoming data in POST/PUT requests.

@PathVariable → Extracts values from the URI path (URL template variables) and binds them to method parameters.

@RequestParam → Extracts query parameters from the URL (after ?) and binds them to method parameters; used for optional or filtering inputs.
```

## Lifecycle

```text
@PostConstruct → Method-level annotation used to execute a method after dependency injection is completed and the bean is fully initialized; typically used for setup/initialization logic.

@PreDestroy → Method-level annotation used to execute a method just before the bean is removed from the Spring container (application shutdown); typically used for cleanup logic like releasing resources.
```

## Conditions

```text
@Profile
@ConditionalOnProperty
@ConditionalOnMissingBean
```
