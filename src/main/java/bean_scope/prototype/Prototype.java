package bean_scope.prototype;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Prototype {

    public static void main(String[] args) {

        // =========================================================
        // PROTOTYPE BEAN SCOPE IN SPRING
        // =========================================================

        /*
         * Prototype scope means:
         *
         * Spring creates a NEW object every time getBean() is called.
         * Unlike singleton scope, Spring does NOT reuse the same object.
         * Every request gets a fresh object.
         */

        // =========================================================
        // LOADING SPRING CONTAINER
        // =========================================================

        /*
         * Spring reads spring.xml
         * ↓
         * Prototype bean definition is stored
         * ↓
         * Object is NOT reused
         * ↓
         * New object created on every getBean()
         */

        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");


        // =========================================================
        // FETCHING SAME BEAN MULTIPLE TIMES
        // =========================================================

        /*
         * bike bean scope = prototype
         *
         * So Spring creates:
         * new object for b1
         * new object for b2
         */

        Bike b1 = context.getBean("bike", Bike.class);
        Bike b2 = context.getBean("bike", Bike.class);


        // =========================================================
        // HASHCODE CHECK
        // =========================================================

        /*
         * Different hashCodes usually mean: different objects in memory.
         */

        System.out.println("c1 hashCode: " + b1.hashCode());
        System.out.println("c2 hashCode: " + b2.hashCode());


        // =========================================================
        // REFERENCE COMPARISON
        // =========================================================

        /*
         * == compares memory references
         *
         * true  -> same object
         * false -> different objects
         */

        System.out.println("Is same object? : " + (b1 == b2));


        // =========================================================
        // MEMORY VISUALIZATION
        // =========================================================

        /*
         * Spring IoC Container
         *
         * getBean("bike")
         *        ↓
         *    Car Object 1  ◄── b1
         *
         *
         * getBean("bike")
         *        ↓
         *    Car Object 2  ◄── b2
         *
         * b1 and b2 point to DIFFERENT objects.
         */


        // =========================================================
        // BENEFITS OF PROTOTYPE SCOPE
        // =========================================================

        /*
         * Fresh object every time
         * Useful for stateful objects
         * Avoids shared mutable state
         */


        // =========================================================
        // REAL-WORLD USE CASES
        // =========================================================

        /*
         * Prototype scope is useful for:
         *
         * User-specific objects
         * Temporary processing objects
         * Request/Session style data
         * Stateful components
         */

    }
}