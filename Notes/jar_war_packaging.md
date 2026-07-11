# JAR vs WAR Packaging

## 1. JAR (Java Archive)

* Contains application classes, resources, and dependencies.
* Executed directly by JVM.
* Spring Boot packages applications as executable JARs by default.
* Includes an embedded server (Tomcat/Jetty/Undertow).

### Build

```xml
<packaging>jar</packaging>
```

### Run

```bash
java -jar app.jar
```

### Flow

```text
JVM
 └─> Main Method
      └─> SpringApplication.run()
           └─> Embedded Tomcat Starts
                └─> Application Ready
```

### Best For

* Microservices
* Docker
* Kubernetes
* Cloud deployments

---

## 2. WAR (Web Application Archive)

* Contains web application resources and classes.
* Deployed to an external servlet container.
* Requires Tomcat/JBoss/WebLogic/WebSphere.

### Build

```xml
<packaging>war</packaging>
```

### Deployment

```text
Tomcat
 └─> app.war
```

### Flow

```text
Tomcat Starts
 └─> WAR Deployed
      └─> Spring Context Created
           └─> Application Ready
```

### Additional Spring Boot Configuration

```java
public class Application extends SpringBootServletInitializer {}
```

```xml
<scope>provided</scope>
```

for embedded Tomcat dependency.

### Best For

* Legacy enterprise applications
* Shared application servers

---

## JAR vs WAR

| Feature                  | JAR                    | WAR              |
|--------------------------|------------------------|------------------|
| Executable               | Yes                    | No (normally)    |
| Embedded Tomcat          | Yes                    | No               |
| External Server Required | No                     | Yes              |
| Spring Boot Default      | Yes                    | No               |
| Microservices            | Preferred              | Rare             |
| Deployment               | `java -jar <jar_path>` | Deploy to Tomcat |

---

## Quick Memory

```text
JAR = Application + Server
WAR = Application only
```

```text
JAR -> Embedded Tomcat
WAR -> External Tomcat
```
