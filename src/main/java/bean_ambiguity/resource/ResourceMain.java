package bean_ambiguity.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ResourceMain {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(ResourceMain.class, args);

        ResourceOrderService orderService = context.getBean(ResourceOrderService.class);

        orderService.placeOrder();
    }
}
