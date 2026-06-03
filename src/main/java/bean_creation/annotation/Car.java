package bean_creation.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component is a stereotype annotation.
//Spring detects it during component scanning and creates a bean and stored it into the container.
//So the bean creation mechanism is Annotation-based configuration.
@Component
public class Car {

    @Value("${car.carType}")
    public String carType;

    @Value("${car.carName}")
    public String carName;

//    Dependency is injected directly into a field.
//    So the DI type is Field Injection (Field-based DI).
    @Autowired
//    @Qualifier("petrolEngine")
    public Engine engine;

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public Car(String carType, String carName, Engine engine) {
        this.carType = carType;
        this.carName = carName;
        this.engine = engine;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public void startCar() {
        System.out.println("Car: Name - " + carName + " , Type - " + carType);
        System.out.println("Engine: Name - " + engine.getEngineName() + " , Type - " + engine.getEngineType());
    }

    Car(){

    }
}
