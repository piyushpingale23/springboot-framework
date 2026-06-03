package bean_creation.java_config.constructor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {

        // --------------------------------------
        // BEFORE SPRING (Manual creation)
        // --------------------------------------

        System.out.println("============ BEFORE SPRING ============");

        Car c1 = new Car("BMW", "Electric");
        Car c2 = new Car("BMW", "Electric");

        System.out.println("c1 hashCode: " + c1.hashCode());
        System.out.println("c2 hashCode: " + c2.hashCode());

        c1.startCar();


        // --------------------------------------
        // AFTER SPRING (IoC Container)
        // --------------------------------------

        System.out.println("\n============ AFTER SPRING ============");

        ApplicationContext context = new AnnotationConfigApplicationContext(SpringBeanConfigConstructor.class);


        // ==========================================
        // Singleton Bean
        // ==========================================

        Car constructor1a = context.getBean("carConstructor1", Car.class);
        Car constructor1b = context.getBean("carConstructor1", Car.class);

        System.out.println("\nSingleton Bean hashCodes:");

        System.out.println("constructor1a: " + constructor1a.hashCode());
        System.out.println("constructor1b: " + constructor1b.hashCode());

        // SAME OBJECT


        // ==========================================
        // Prototype Bean
        // ==========================================

        Car constructor2a = context.getBean("carConstructor2", Car.class);
        Car constructor2b = context.getBean("carConstructor2", Car.class);

        System.out.println("\nPrototype Bean hashCodes:");

        System.out.println("constructor2a: " + constructor2a.hashCode());
        System.out.println("constructor2b: " + constructor2b.hashCode());

        // DIFFERENT OBJECTS


        // ==========================================
        // METHOD CALL TESTING
        // ==========================================

        constructor1a.startCar();
        constructor2a.startCar();
    }
}