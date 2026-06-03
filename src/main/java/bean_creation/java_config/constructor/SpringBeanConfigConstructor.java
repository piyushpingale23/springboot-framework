package bean_creation.java_config.constructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SpringBeanConfigConstructor {

    // ==========================================
    // Singleton Bean (Default Scope)
    // ==========================================

    @Bean
    public Car carConstructor1() {
        return new Car("BMW", "Electric");
    }

    // ==========================================
    // Prototype Bean
    // ==========================================

    @Bean
    @Scope("prototype")
    public Car carConstructor2() {
        return new Car("Audi", "Petrol");
    }

}