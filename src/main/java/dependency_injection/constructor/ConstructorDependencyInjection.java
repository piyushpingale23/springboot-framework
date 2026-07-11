package dependency_injection.constructor;

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
public class ConstructorDependencyInjection {

    private final Engine engine;

    // Constructor-Based Dependency Injection
    public ConstructorDependencyInjection(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        System.out.println("Car is ready to drive.");
        engine.start();
    }
}

//Visual Flow:
//Need ConstructorDependencyInjection Bean
//                │
//                ▼
//Constructor requires Engine
//                │
//                ▼
//Check Engine Bean Exists?
//        │
//No ───► Create Engine
//                │
//                ▼
//Pass Engine into Constructor
//                │
//                ▼
//Create ConstructorDependencyInjection Bean
//                │
//                ▼
//Store Both Beans in Spring Container