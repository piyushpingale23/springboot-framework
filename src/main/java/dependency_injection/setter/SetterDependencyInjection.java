package dependency_injection.setter;

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
public class SetterDependencyInjection {

    private Engine engine;

    // Setter-Based Dependency Injection
    @Autowired
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        System.out.println("Car is ready to drive.");
        engine.start();
    }
}

//Visual Flow:
//Need SetterDependencyInjection Bean
//                │
//                ▼
//Create SetterDependencyInjection Object
//        (using default constructor)
//                │
//                ▼
//Check @Autowired Setter Methods
//                │
//                ▼
//Setter requires Engine
//                │
//                ▼
//Check Engine Bean Exists?
//        │
//No ───► Create Engine
//                │
//                ▼
//Call setEngine(engine)
//                │
//                ▼
//Inject Engine into Bean
//                │
//                ▼
//Store Both Beans in Spring Container