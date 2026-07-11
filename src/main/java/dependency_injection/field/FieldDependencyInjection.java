package dependency_injection.field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Dependency Class
@Component
class Engine {

    public void start() {
        System.out.println("Engine Started...");
    }
}

// Dependent Class
@Component
public class FieldDependencyInjection {

    // Field-Based Dependency Injection
    //
    // @Autowired tells Spring to inject an Engine bean here.
    //
    // Since this field is private, Spring cannot access it directly.
    // Spring uses Java Reflection internally to inject the dependency.
    //
    // Conceptually, Spring does something similar to:
    //
    // Field field = FieldDependencyInjection.class.getDeclaredField("engine");
    //
    // field.setAccessible(true);
    // field.set(this, engineBean);
    //
    // Reflection allows Spring to access and modify private fields at runtime.
    //
    // This is one reason Constructor Injection is preferred over
    // Field Injection in modern Spring applications.
    @Autowired
    private Engine engine;

    public void drive() {
        System.out.println("Car is ready to drive.");
        engine.start();
    }
}