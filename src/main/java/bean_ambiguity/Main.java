package bean_ambiguity;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        /*
         * Activate profile before refresh().
         */
        context.getEnvironment().setActiveProfiles("dev");
        context.register(BeanConfig.class);
        context.refresh();

        VehicleRunner runner = context.getBean(VehicleRunner.class);
        runner.test();

        /*
         * Verify Profile Bean
         */
        Vehicle testVehicle = context.getBean("testVehicle", Vehicle.class);

        System.out.println();
        System.out.println("=== @Profile Example ===");
        testVehicle.move();

        context.close();
    }
}