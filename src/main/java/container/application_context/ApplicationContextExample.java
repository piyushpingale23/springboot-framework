package container.application_context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/*
 * ApplicationContext Example
 * --------------------------
 * ApplicationContext is an advanced Spring container.
 *
 * Important Point:
 * Singleton beans are created EAGERLY.
 *
 * Steps:
 * 1. ApplicationContext loads XML.
 * 2. Spring immediately creates singleton beans.
 * 3. getBean() only returns the already created bean.
 */
public class ApplicationContextExample {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

        /*
         * Engine bean is already created here.
         */

        System.out.println("ApplicationContext Created");

        Engine engine = context.getBean("engine_application_context", Engine.class);

        engine.start();
    }
}

class Engine {

    public Engine() {
        System.out.println("ApplicationContext Engine Bean Created");
    }

    public void start() {
        System.out.println("ApplicationContext Engine Started");
    }
}