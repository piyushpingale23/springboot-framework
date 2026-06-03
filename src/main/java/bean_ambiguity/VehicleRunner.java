package bean_ambiguity;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/*
 * Demonstrates:
 * 1. @Primary
 * 2. @Qualifier
 * 3. @Resource
 */
public class VehicleRunner {

    /*
     * Spring injects Car because it is marked @Primary.
     */
    @Autowired
    private Vehicle primaryVehicle;

    /*
     * Spring injects Bike because of @Qualifier.
     */
    @Autowired
    @Qualifier("bike")
    private Vehicle qualifierVehicle;

    /*
     * Spring injects Car by bean name.
     */
    @Resource(name = "car")
    private Vehicle resourceVehicle;

    public void test() {

        System.out.println("=== @Primary Example ===");
        primaryVehicle.move();

        System.out.println();

        System.out.println("=== @Qualifier Example ===");
        qualifierVehicle.move();

        System.out.println();

        System.out.println("=== @Resource Example ===");
        resourceVehicle.move();
    }
}
