package api.actuator;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;
import java.util.Map;

// http://localhost:8090/actuator/app-version
@WebEndpoint(id = "app-version")
@Component
public class CustomWebEndpoint {

    @ReadOperation
    public Map<String, Object> info() {
        return Map.of(
                "version", "Spring Boot - v1.0",
                "status", "UP"
        );
    }
}
