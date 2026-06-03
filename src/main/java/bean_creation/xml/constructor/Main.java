package bean_creation.xml.constructor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {

        // -----------------------------
        // BEFORE SPRING (Manual creation)
        // -----------------------------
        System.out.println("============ BEFORE SPRING (Manual creation) ============");

        Car c1 = new Car();
        Car c2 = new Car("BMW", "Electric");
        Car c3 = new Car("BMW", "Electric");

        System.out.println("c1 hashCode: " + c1.hashCode());
        System.out.println("c2 hashCode: " + c2.hashCode());
        System.out.println("c3 hashCode: " + c3.hashCode());

        c1.startCar();
        c2.startCar();


        // -----------------------------
        // AFTER SPRING (IoC Container)
        // -----------------------------
        System.out.println("============ AFTER SPRING (IoC Container) ============");


        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");

        // =====================================================
        // CONSTRUCTOR-BASED INJECTION BEANS
        // =====================================================

        // carConstructor1 -> Singleton scope (default)
        Car constructor1a = applicationContext.getBean("carConstructor1", Car.class);
        Car constructor1b = applicationContext.getBean("carConstructor1", Car.class);

        System.out.println("\nConstructor Bean (Singleton) hashCodes:");
        System.out.println("constructor1a: " + constructor1a.hashCode());
        System.out.println("constructor1b: " + constructor1b.hashCode());
        // SAME OBJECT → singleton


        // carConstructor2 → Prototype scope
        Car constructor2a = applicationContext.getBean("carConstructor2", Car.class);
        Car constructor2b = applicationContext.getBean("carConstructor2", Car.class);

        System.out.println("\nConstructor Bean (Prototype) hashCodes:");
        System.out.println("constructor2a: " + constructor2a.hashCode());
        System.out.println("constructor2b: " + constructor2b.hashCode());
        // DIFFERENT OBJECTS → prototype


        // =====================================================
        // METHOD CALL TESTING
        // =====================================================

        constructor1a.startCar();
        constructor2a.startCar();
    }
}