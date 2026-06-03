package bean_ambiguity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class BeanConfig {

    /*
     * Bean Name = car
     * Default bean because of @Primary
     */
    @Bean
    @Primary
    public Vehicle car() {
        return new Car();
    }

    /*
     * Bean Name = bike
     */
    @Bean
    public Vehicle bike() {
        return new Bike();
    }

    /*
     * Created only when profile = dev
     */
    @Bean
    @Profile("dev")
    public Vehicle testVehicle() {
        return new TestVehicle();
    }

    @Bean
    public VehicleRunner vehicleRunner() {
        return new VehicleRunner();
    }
}
