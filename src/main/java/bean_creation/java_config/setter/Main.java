package bean_creation.java_config.setter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {

        System.out.println("============ AFTER SPRING (Setter DI) ============");

        ApplicationContext context = new AnnotationConfigApplicationContext(SpringBeanConfigSetter.class);

        Car car = context.getBean("carSetter1", Car.class);

        car.startCar();
    }
}