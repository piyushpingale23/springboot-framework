package annotations.request_response;

import annotations.request_response.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/annotations")
public class SpringBootAnnotationsController {

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
    @PostMapping("/employees")
    public String createEmployee(@ModelAttribute User user) {
        return "Employee Created: " + user.getName();
    }
}