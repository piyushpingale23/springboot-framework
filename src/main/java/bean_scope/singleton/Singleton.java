package bean_scope.singleton;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Singleton {

    public static void main(String[] args) {

        // =========================================================
        // SINGLETON BEAN SCOPE IN SPRING
        // =========================================================

        /*
         * Singleton is the DEFAULT bean scope in Spring.
         *
         * Meaning:
         * Spring creates ONLY ONE object per bean ID inside the IoC Container.
         *
         * Every time getBean() is called for the same bean ID,
         * Spring returns the SAME object reference.
         *
         * IMPORTANT:
         * Singleton in Spring means:
         * "One object per bean definition (bean ID)"
         *
         * It DOES NOT mean:
         * "One object per class in entire JVM"
         */


        // =========================================================
        // LOADING SPRING CONTAINER
        // =========================================================

        /*
         * Spring reads spring.xml
         * ↓
         * Creates bean objects
         * ↓
         * Stores singleton beans inside IoC Container
         */

        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");

        // =========================================================
        // FETCHING SAME BEAN MULTIPLE TIMES
        // =========================================================

        /*
         * car bean scope = singleton
         *
         * So Spring will NOT create new object on every getBean() call.
         *
         * Instead: same object reference is returned.
         */

        Car c1 = context.getBean("car", Car.class);
        Car c2 = context.getBean("car", Car.class);


        // =========================================================
        // HASHCODE CHECK
        // =========================================================

        /*
         * Same hashCode usually means: same object reference in memory.
         */

        System.out.println("c1 hashCode: " + c1.hashCode());
        System.out.println("c2 hashCode: " + c2.hashCode());


        // =========================================================
        // REFERENCE COMPARISON
        // =========================================================

        /*
         * == compares memory references
         *
         * true  -> same object
         * false -> different objects
         */

        System.out.println("Is same object? : " + (c1 == c2));


        // =========================================================
        // MEMORY VISUALIZATION
        // =========================================================

        /*
         * Spring IoC Container
         *
         * ┌─────────────────────┐
         * │  car bean           │
         * │      ↓              │
         * │   Car Object        │
         * └─────────────────────┘
         *
         * c1 ───────────────┐
         *                   │
         * c2 ───────────────┘
         *
         * Both c1 and c2 point to SAME object.
         */


        // =========================================================
        // BENEFITS OF SINGLETON SCOPE
        // =========================================================

        /*
         * Memory efficient
         * Faster object reuse
         * Best for stateless services
         * Default Spring scope
         */

    }
}