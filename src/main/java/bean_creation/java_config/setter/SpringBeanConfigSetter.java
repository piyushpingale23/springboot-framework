package bean_creation.java_config.setter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBeanConfigSetter {

    @Bean
    // Spring create bean with name carSetter1, and store it in IOC container
    public Car carSetter1() {

        Car car = new Car(); // Step 1: Object creation (no constructor args)

        // Step 2: Setter injection
        car.setCarModel("BMW");
        car.setCarType("Electric");

        return car; // Step 3: return bean to Spring container
    }

    @Bean
    public Car carSetter2() {
        Car car = new Car();  // Step 1: Object creation

        // Step 2: Setter injection
        car.setCarModel("Jaguar");
        car.setCarType("Petrol");

        return car; // Step 3: return bean to Spring container
    }
}