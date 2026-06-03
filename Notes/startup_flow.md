# Spring Boot Application Startup Flow

## Complete Startup Sequence

```text
main()
   ↓
SpringApplication.run()
   ↓
Create Environment
   ↓
Load application.properties / application.yml
   ↓
Create ApplicationContext
   ↓
Component Scan
   ↓
Create Bean Definitions
   ↓
Register BeanPostProcessors
   ↓
Create Singleton Beans
   ↓
Constructor Execution
   ↓
Dependency Injection (@Autowired)
   ↓
@Value Injection
   ↓
Aware Interfaces
   ↓
BeanPostProcessor Before Initialization
   ↓
@PostConstruct
   ↓
afterPropertiesSet()
   ↓
Custom Init Method
   ↓
BeanPostProcessor After Initialization
   ↓
AOP Proxy Creation
   ↓
All Singleton Beans Ready
   ↓
Start Embedded Tomcat
   ↓
ApplicationStartedEvent
   ↓
ApplicationReadyEvent
   ↓
Application Accepting Requests
```

---

## 1. JVM Starts Application

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- JVM loads the Application class.
- `main()` method is executed.
- Spring Boot startup begins.

---

## 2. SpringApplication Starts

```
SpringApplication.run(Application.class, args);
```

Spring Boot:

- Creates a `SpringApplication` object.
- Determines application type (Servlet, Reactive, Non-Web).
- Registers listeners and initializers.

---

## 3. Environment Creation

Spring creates an `Environment` object.

Configuration sources are loaded:

1. application.properties
2. application.yml
3. Environment Variables
4. JVM System Properties
5. Command Line Arguments

Example:

```properties
server.port=8080
app.name=EmployeeApp
```

At this stage configuration values become available.

---

## 4. ApplicationContext Creation

Spring creates the IOC Container.

For web applications:

```text
ServletWebServerApplicationContext
```

This container manages all Spring beans.

---

## 5. Component Scanning

`@SpringBootApplication` contains:

```
@ComponentScan
```

Spring scans packages for:

```
@Component
@Service
@Repository
@Controller
@RestController
@Configuration
```

At this stage:

- Bean Definitions are created.
- Bean objects are not created yet.

Example:

```java
@Service
public class UserService {
}
```

Bean Definition:

```text
Bean Name : userService
Bean Type : UserService
Scope     : Singleton
```

---

## 6. BeanFactory Prepared

Bean definitions are stored inside BeanFactory.

```text
Bean Definitions = Available
Bean Objects     = Not Created Yet
```

---

## 7. BeanPostProcessor Registration

Spring registers internal processors responsible for handling:

```
@Autowired
@Value
@PostConstruct
```

Examples:

```text
AutowiredAnnotationBeanPostProcessor
CommonAnnotationBeanPostProcessor
```

---

## 8. Singleton Bean Creation Starts

Spring starts creating singleton beans.

Example:

```java
@Service
public class UserService {
}
```

Spring executes:

```
new UserService();
```

Constructor is called first.

---

## 9. Dependency Injection

Spring injects dependencies.

Example:

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;
}
```

Flow:

```text
Create UserRepository
Create UserService
Inject UserRepository into UserService
```

---

## 10. @Value Injection

Spring injects property values loaded earlier from Environment.

Example:

```java
@Value("${app.name}")
private String appName;
```

```properties
app.name=EmployeeApp
```

Result:

```
appName = "EmployeeApp";
```

---

## 11. Aware Interface Callbacks

If implemented, Spring invokes:

```
BeanNameAware
BeanFactoryAware
ApplicationContextAware
EnvironmentAware
```

Examples:

```java
setBeanName();
setBeanFactory();
setApplicationContext();
```

---

## 12. BeanPostProcessor Before Initialization

Spring invokes:

```java
postProcessBeforeInitialization();
```

for all registered BeanPostProcessors.

---

## 13. @PostConstruct

Spring executes methods annotated with:

```java
@PostConstruct
public void init() {
    System.out.println("Initializing...");
}
```

At this point:

- Constructor completed
- Dependencies injected
- Properties injected

Sequence:

```text
Constructor
   ↓
@Autowired
   ↓
@Value
   ↓
@PostConstruct
```

---

## 14. InitializingBean

If bean implements:

```
InitializingBean
```

Spring invokes:

```java
afterPropertiesSet();
```

---

## 15. Custom Init Method

If configured:

```
@Bean(initMethod = "init")
```

Spring invokes:

```java
init();
```

---

## 16. BeanPostProcessor After Initialization

Spring invokes:

```java
postProcessAfterInitialization();
```

This is where Spring often creates proxies for:

```
@Transactional
@Async
@Cacheable
```

---

## 17. All Singleton Beans Ready

At this stage:

```text
All Singleton Beans Created
Dependencies Injected
@PostConstruct Executed
Proxies Created
```

IOC Container initialization is complete.

---

## 18. Embedded Tomcat Starts

Spring Boot starts the embedded web server.

Usually:

```text
Tomcat
```

Example log:

```text
Tomcat started on port(s): 8080
```

Server begins listening for requests.

---

## 19. Application Events

Spring publishes startup events:

```
ApplicationStartedEvent
```

followed by:

```
ApplicationReadyEvent
```

Example:

```java
@EventListener(ApplicationReadyEvent.class)
public void ready() {
    System.out.println("Application Ready");
}
```

This executes after the application is fully started.

---

## 20. Application Running

At this point:

```text
Tomcat Listening
Controllers Ready
Services Ready
Repositories Ready
```

Application is ready to process requests.

---

# Single Bean Lifecycle

```text
Constructor
   ↓
Dependency Injection
   ↓
@Value Injection
   ↓
Aware Interfaces
   ↓
BeanPostProcessor Before Init
   ↓
@PostConstruct
   ↓
afterPropertiesSet()
   ↓
Custom Init Method
   ↓
BeanPostProcessor After Init
   ↓
Bean Ready
```

---

# Application Shutdown Flow

```text
Application Shutdown
   ↓
Tomcat Stops
   ↓
@PreDestroy
   ↓
destroy()
   ↓
Custom destroyMethod
   ↓
Bean Removed
   ↓
JVM Exit
```