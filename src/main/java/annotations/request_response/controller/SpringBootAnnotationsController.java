package annotations.request_response.controller;

import annotations.request_response.config.AppProperties;
import annotations.request_response.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/annotations")
public class SpringBootAnnotationsController {

    @Autowired
    AppProperties appProperties;

    @Autowired(required = false)
    private String loggingFeature;

    // @Value is used to inject a value from a properties file, environment variable, system property, or Spring Expression Language (SpEL) into a Spring-managed bean.
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    // http://localhost:8090/annotations/welcome
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Spring Boot Annotations!";
    }

    // http://localhost:8090/annotations/users/4
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Long id) {
        return "User Id: " + id;
    }

    // http://localhost:8090/annotations/search?name=Spring-Boot-Annotations
    @GetMapping("/search")
    public String searchUser(@RequestParam String name) {
        return "Searching user: " + name;
    }

    // http://localhost:8090/annotations/employees?id=1&department=IT
    @GetMapping("/employees")
    public String getEmployee(@RequestParam Long id, @RequestParam String department) {
        return "Employee Id: " + id +
                ", Department: " + department;
    }

    // curl -X POST http://localhost:8090/annotations/users -H "Content-Type: application/json" -d "{\"id\":101,\"name\":\"RequestBody\"}"
    @PostMapping("/users")
    public String createUser(@RequestBody User user) {
        return "User Created: " + user.getName();
    }

    // curl -X POST http://localhost:8090/annotations/employees -d "id=101&name=ModelAttribute"
    // here we use @RequestParam also.
    // @RequestParam -> Binds individual request parameters to method arguments.
    // @ModelAttribute -> Binds multiple request parameters/form fields to a Java object.
    // @ModelAttribute does not deserialize JSON. Instead,
    // Spring creates an empty object and populates its fields from request parameters using its data binding mechanism (reflection + setters/type conversion).
    // User user = new User();
    // user.setId(101); & user.setName("ModelAttribute");
    // And internally via reflection,
    // User {
    //    id = 101
    //    name = "ModelAttribute"
    //}
    @PostMapping("/employees")
    public String createEmployee(@ModelAttribute User user
//                                  @RequestParam Long id, @RequestParam String name
                                  ) {
        return "Employee Created: " + user.getName();
    }

    // http://localhost:8090/annotations/application-info
    @GetMapping("/application-info")
    public String applicationInfo() {
        return "Application Name: " + appName + ", Version: " + appVersion;
    }

    // http://localhost:8090/annotations/application-info-config
    @GetMapping("/application-info-config")
    public String applicationInfoConfig() {
        return "Application Name: " + appProperties.getName() +
                ", Version: " + appProperties.getVersion() +
                ", Owner: " + appProperties.getOwner();
    }

    // http://localhost:8090/annotations/feature-status
    @GetMapping("/feature-status")
    public String featureStatus() {

        if (loggingFeature != null) {
            return loggingFeature;
        }
        return "Logging Feature is DISABLED";

    }
}