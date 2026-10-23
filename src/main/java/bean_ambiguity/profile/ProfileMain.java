package bean_ambiguity.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ProfileMain {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(ProfileMain.class, args);

        ProfileOrderService orderService = context.getBean(ProfileOrderService.class);

        orderService.placeOrder();
    }
}
