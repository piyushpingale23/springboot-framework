# Spring Boot Annotations

---

# 1. Core Spring Framework Annotations

## Component Scanning and Bean Definition

| Annotation        | Description                                                                              |
|-------------------|------------------------------------------------------------------------------------------|
| `@Component`      | Marks a class as a Spring-managed bean detected automatically during component scanning. |
| `@Controller`     | Specialization of `@Component` used for Spring MVC controllers.                          |
| `@Service`        | Specialization of `@Component` used for service/business-logic classes.                  |
| `@Repository`     | Specialization of `@Component` used for DAO/data-access classes.                         |
| `@RestController` | Combines `@Controller` + `@ResponseBody`, commonly used for REST APIs.                   |
| `@Configuration`  | Marks a class as a configuration class containing bean definitions.                      |
| `@Bean`           | Registers the object returned by a method as a Spring bean.                              |

## `@Component`

Marks a class as a Spring-managed bean.

```
@Component
public class EmailService {

    public void sendEmail() {
        System.out.println("Email sent");
    }
}
```

Spring detects `EmailService` during component scanning and creates its bean.

## `@Service`

Specialization of `@Component`, mainly used for business logic.

```
@Service
public class PaymentService {

    public void processPayment() {
        System.out.println("Payment processed");
    }
}
```

## `@Repository`

Specialization of `@Component`, mainly used for data-access/database classes.

```
@Repository
public class PaymentRepository {

    public Payment findById(Long id) {
        // Database operation
        return new Payment();
    }
}
```

`@Repository` also enables Spring's persistence exception translation.

## `@Controller`

Used for Spring MVC controllers, generally when returning a view.

```
@Controller
public class PaymentController {

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }
}
```

## `@RestController`

Used for REST APIs.

```
@RestController
public class PaymentController {

    @GetMapping("/payment")
    public Payment getPayment() {
        return new Payment();
    }
}
```

Conceptually:

```
@RestController = @Controller + @ResponseBody
```

The returned Java object is written directly to the HTTP response, usually as JSON.

---

# 2. `@Configuration` and `@Bean`

## `@Configuration`

Marks a class as a configuration class containing bean definitions.

```
@Configuration
public class AppConfig {
}
```

## `@Bean`

Marks a method whose return value should be registered as a Spring bean.

```
@Configuration
public class AppConfig {

    @Bean
    public PaymentService paymentService() {
        return new PaymentService();
    }
}
```

Flow:

```
@Configuration
      ↓
@Bean method
      ↓
PaymentService object created
      ↓
Registered in Spring IoC Container
```

### When to use `@Bean`?

Use `@Bean` when:

* You cannot modify the class to add `@Component`.
* You need custom object configuration.

---

# 3. Spring MVC Annotations

## `@RequestMapping`

Maps an HTTP request to a controller class or method.

**Used on:** Class or method.

```
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @RequestMapping("/process")
    public String process() {
        return "Payment processed";
    }
}
```

Request:

```
GET /payments/process
```

---

## `@ResponseBody`

Tells Spring to write the return value directly to the HTTP response body.

```
@Controller
public class PaymentController {

    @ResponseBody
    @GetMapping("/payment")
    public Payment getPayment() {
        return new Payment();
    }
}
```

`@RestController` already provides `@ResponseBody` behavior.

---

## `@RequestBody`

It maps the HTTP request body, usually JSON, to a Java object.

```
@PostMapping("/payment")
public String createPayment(@RequestBody Payment payment) {
    return "Payment created";
}
```

Request:

```
{
    "amount": 1000,
    "type": "CASH"
}
```

Flow:

```
JSON Request Body
       ↓
  @RequestBody
       ↓
Payment Java Object
```

---

## `@ModelAttribute`

Binds request/form parameters to a Java object.

```
@PostMapping("/payment")
public String createPayment(@ModelAttribute Payment payment) {
    return "Payment created";
}
```

Example form data:

```
amount=1000
type=CASH
```

---

## `@PathVariable`

Extracts a value from the URL path.

```
@GetMapping("/payment/{id}")
public Payment getPayment(@PathVariable Long id) {
    return paymentService.getPayment(id);
}
```

Request:

```
GET /payment/10
```

Then:

```
id = 10
```

---

## `@RequestParam`

Reads query parameters.

```
@GetMapping("/payment")
public Payment getPayment(@RequestParam Long id) {
    return paymentService.getPayment(id);
}
```

Request:

```
GET /payment?id=10
```

Then:

```
id = 10
```

### Difference

```
@PathVariable
/payment/10

@RequestParam
/payment?id=10
```

---

## `@RequestHeader`

Extracts a value from an HTTP request header.

```
@GetMapping("/payment")
public String getPayment(
        @RequestHeader("Authorization") String token) {

    return token;
}
```

Request header:

```
Authorization: Bearer abc123
```

Then:

```
token = "Bearer abc123"
```

---

# 4. Dependency Injection

| Annotation   | Description                                                               |
|--------------|---------------------------------------------------------------------------|
| `@Autowired` | Injects a Spring bean, primarily by type.                                 |
| `@Qualifier` | Selects a specific bean when multiple beans of the same type exist.       |
| `@Primary`   | Marks one bean as the default when multiple beans of the same type exist. |
| `@Value`     | Injects values from configuration/properties or literal values.           |
| `@Resource`  | Injects a bean primarily by name.                                         |

## `@Autowired`

Injects a Spring-managed bean.

```
@Service
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

With a single constructor, `@Autowired` can be omitted:

```
public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
}
```

Spring finds the `PaymentService` bean and injects it.

---

## `@Value`

Injects a configuration/property value.

`application.properties`:

```
app.name=Core Service
app.version=1.0
```

Java:

```
@Service
public class AppService {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String version;
}
```

Values:

```
appName = "Core Service"
version = "1.0"
```

---

# 5. Spring Boot Annotations

| Annotation                       | Description                                                                                              |
|----------------------------------|----------------------------------------------------------------------------------------------------------|
| `@SpringBootApplication`         | Combines `@SpringBootConfiguration`, `@EnableAutoConfiguration`, and `@ComponentScan`.                   |
| `@SpringBootConfiguration`       | Marks the main Spring Boot configuration class.                                                          |
| `@EnableAutoConfiguration`       | Automatically configures beans based on classpath dependencies and application configuration.            |
| `@ComponentScan`                 | Scans packages for Spring components such as `@Component`, `@Service`, `@Repository`, and `@Controller`. |
| `@PropertySource`                | Loads a custom properties file into Spring's `Environment`.                                              |
| `@ConfigurationProperties`       | Binds a group of configuration properties to a Java object.                                              |
| `@EnableConfigurationProperties` | Registers/enables `@ConfigurationProperties` classes as Spring beans.                                    |
| `@ConditionalOnProperty`         | Creates a bean based on a property value/existence.                                                      |
| `@ConditionalOnMissingBean`      | Creates a bean only when another matching bean does not already exist.                                   |
| `@SpringBootTest`                | Used for Spring Boot integration testing.                                                                |
| `@TestConfiguration`             | Provides additional configuration specifically for tests.                                                |

---

# 6. `@SpringBootApplication`

`@SpringBootApplication` is a combination of:

```
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

---

# 7. `@SpringBootConfiguration`

Marks the class as the main Spring Boot configuration class.

Conceptually:

```
@SpringBootConfiguration ≈ @Configuration
```

---

# 8. `@EnableAutoConfiguration`

Tells Spring Boot:

```
"Automatically configure the application based on
 the libraries available on the classpath and
 application configuration."
```

Example:

If the project contains:

```
spring-boot-starter-web
```

Spring Boot can automatically configure infrastructure such as:

```
Embedded Tomcat
Spring MVC
DispatcherServlet
Jackson
HTTP message converters
```

If the project contains JPA/database dependencies and database configuration, Spring Boot can also autoconfigure:

```
DataSource
EntityManagerFactory
Transaction infrastructure
```

Important:

```
Starter dependency
     ↓
Adds libraries to classpath
     ↓
@EnableAutoConfiguration
     ↓
Detects available libraries/configuration
     ↓
Creates appropriate Spring beans
```

The starter itself does not perform the autoconfiguration.

---

# 9. `@ComponentScan`

Tells Spring which packages to scan for component classes.

Example:

```
@ComponentScan("com.myapp")
public class AppConfig {
}
```

Spring scans the package and subpackages for:

```
@Component
@Service
@Repository
@Controller
@RestController
```

and registers them as beans.

### Default behavior

With:

```
@SpringBootApplication
```

component scanning normally starts from the package containing the main application class and scans its subpackages.

Example:

```
com.myapp
├── MyApplication.java
├── controller
├── service
└── repository
```

If `MyApplication` is in `com.myapp`, the subpackages are scanned automatically.

---

# 10. `@PropertySource`

Loads a custom `.properties` file into Spring's `Environment`.

Example:

```
src/main/resources/custom.properties
```

Contents:

```
app.name=Core Service
```

Configuration:

```
@Configuration
@PropertySource("classpath:custom.properties")
public class AppConfig {
}
```

Now the property can be accessed using:

```
@Value("${app.name}")
private String appName;
```

### Important Points

Spring Boot automatically loads:

```
application.properties
application.yml
```

and profile-specific files such as:

```
application-dev.properties
application-prod.properties
```

Custom files such as:

```
custom.properties
```

are not automatically loaded just because they are inside `src/main/resources`.

Use:

```
@PropertySource("classpath:custom.properties")
```

to load them.

### `classpath:`

```
@PropertySource("classpath:custom.properties")
```

means Spring looks for:

```
custom.properties
```

on the application's runtime classpath.

For a standard project:

```
src/main/resources/custom.properties
                 ↓
            build output
                 ↓
             classpath
                 ↓
         custom.properties
```

---

# 11. `@ConfigurationProperties`

Used to bind a group of related configuration properties to a Java object.

`application.properties`:

```
app.name=Core Service
app.version=1.0
app.owner=Piyush
```

Java:

```
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String version;
    private String owner;

    // getters and setters
}
```

Binding:

```
app.name
   ↓
name

app.version
   ↓
version

app.owner
   ↓
owner
```

Result:

```
AppProperties
    ├── name = "Core Service"
    ├── version = "1.0"
    └── owner = "Piyush"
```

### Why use `@ConfigurationProperties`?

Instead of:

```
@Value("${app.name}")
@Value("${app.version}")
@Value("${app.owner}")
```

you can group related configuration into one object.

---

# 12. `@EnableConfigurationProperties`

Used to enable/register a `@ConfigurationProperties` class as a Spring bean.

Example:

```
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;

    // getter/setter
}

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
}
```

Now `AppProperties` can be injected:

```
@Service
public class AppService {

    private final AppProperties properties;

    public AppService(AppProperties properties) {
        this.properties = properties;
    }
}
```

In Spring Boot applications, `@ConfigurationProperties` classes can also be registered using other mechanisms such as `@ConfigurationPropertiesScan`.

---

# 13. Conditional Annotations

Spring Boot provides conditional annotations to decide whether a bean should be created.

Common examples:

```
@ConditionalOnProperty
@ConditionalOnMissingBean
@ConditionalOnBean
@ConditionalOnClass
@ConditionalOnMissingClass
```

These are heavily used internally by Spring Boot autoconfiguration.

---

# 14. `@ConditionalOnProperty`

Creates a bean only when a specified property matches a condition.

`application.properties`:

```
feature.email.enabled=true
```

Java:

```
@Configuration
public class AppConfig {

    @Bean
    @ConditionalOnProperty(
        name = "feature.email.enabled",
        havingValue = "true"
    )
    public EmailService emailService() {
        return new EmailService();
    }
}
```

If:

```
feature.email.enabled=true
```

then:

```
EmailService bean → CREATED
```

If:

```
feature.email.enabled=false
```

then:

```
EmailService bean → NOT CREATED
```

### Useful for feature toggles

Example:

```
feature.sms.enabled=true
feature.email.enabled=false
```

This allows features/beans to be enabled or disabled through configuration without changing Java code.

---

# 15. `@ConditionalOnMissingBean`

Creates a bean only when a matching bean does not already exist.

Example:

```
@Bean
@ConditionalOnMissingBean
public PaymentService paymentService() {
    return new PaymentService();
}
```

Meaning:

```
PaymentService already exists?
      ↓
   YES → Do not create this bean
   NO  → Create this bean
```

This is commonly used by Spring Boot autoconfiguration so that user-defined beans can override default beans.

---

# 16. Testing Annotations

## `@SpringBootTest`

Used for Spring Boot integration testing.

```
@SpringBootTest
class PaymentServiceTest {

    @Test
    void testPayment() {
        // Test
    }
}
```

It loads the Spring application context for the test.

---

## `@TestConfiguration`

Provides additional configuration specifically for tests.

```
@TestConfiguration
public class TestConfig {

    @Bean
    public PaymentService paymentService() {
        return new PaymentService();
    }
}
```

Useful when a test needs additional or customized beans without changing the main application configuration.

---

# 17. Quick Revision

## Bean Creation

```
@Component
@Service
@Repository
@Controller
@RestController
@Bean
```

## Configuration

```
@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
@PropertySource
```

## Dependency Injection

```
@Autowired
@Qualifier
@Primary
@Resource
@Value
```

## Spring MVC

```
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@RequestBody
@ResponseBody
@PathVariable
@RequestParam
@RequestHeader
@ModelAttribute
```

## Spring Boot

```
@SpringBootApplication
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

## Conditional Configuration

```
@ConditionalOnProperty
@ConditionalOnMissingBean
@ConditionalOnBean
@ConditionalOnClass
```

## Testing

```
@SpringBootTest
@TestConfiguration
```

---

# 18. Most Important Relationships to Remember

```
@SpringBootApplication
         =
@SpringBootConfiguration
       +
@EnableAutoConfiguration
       +
@ComponentScan

@RestController
         =
@Controller
       +
@ResponseBody

@Service
         extends
@Component conceptually

@Repository
         extends
@Component conceptually

@Controller
         extends
@Component conceptually

@RestController
         extends
@Controller conceptually

@Bean
→ Method-level bean definition

@Component
→ Class-level component scanning
```

---

# 19. One-Line Interview Revision

```
@Component
→ Generic Spring-managed component.

@Service
→ Business/service layer bean.

@Repository
→ Data-access layer bean.

@Controller
→ MVC controller returning views.

@RestController
→ REST controller returning response bodies.

@Configuration
→ Configuration class containing bean definitions.

@Bean
→ Manually register a method's return object as a Spring bean.

@Autowired
→ Inject bean primarily by type.

@Qualifier
→ Explicitly select a bean.

@Primary
→ Mark the default bean among multiple candidates.

@Resource
→ Inject bean primarily by name.

@Value
→ Inject configuration/literal values.

@RequestBody
→ HTTP body → Java object.

@ResponseBody
→ Java return value → HTTP response body.

@PathVariable
→ Extract value from URL path.

@RequestParam
→ Extract query parameter.

@RequestHeader
→ Extract HTTP header.

@ConfigurationProperties
→ Bind a group of properties to an object.

@ConditionalOnProperty
→ Create bean based on property condition.

@SpringBootApplication
→ Main Spring Boot configuration annotation.

@EnableAutoConfiguration
→ Automatically configure beans based on classpath/configuration.

@ComponentScan
→ Find component classes and register them as beans.
```
