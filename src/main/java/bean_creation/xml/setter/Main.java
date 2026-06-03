package bean_creation.xml.setter;

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
        // SETTER-BASED INJECTION BEANS
        // =====================================================

        // carSetter1 -> Singleton (default scope)
        Car setter1a = applicationContext.getBean("carSetter1", Car.class);
        Car setter1b = applicationContext.getBean("carSetter1", Car.class);

        System.out.println("Setter Bean (Singleton) hashCodes:");
        System.out.println("setter1a: " + setter1a.hashCode());
        System.out.println("setter1b: " + setter1b.hashCode());
        // SAME OBJECT → because singleton scope


        // carSetter2 -> Prototype scope
        Car setter2a = applicationContext.getBean("carSetter2", Car.class);
        Car setter2b = applicationContext.getBean("carSetter2", Car.class);

        System.out.println("\nSetter Bean (Prototype) hashCodes:");
        System.out.println("setter2a: " + setter2a.hashCode());
        System.out.println("setter2b: " + setter2b.hashCode());
        // DIFFERENT OBJECTS → because prototype scope

        // =====================================================
        // METHOD CALL TESTING
        // =====================================================

        setter1a.startCar();
        setter2a.startCar();
    }
}