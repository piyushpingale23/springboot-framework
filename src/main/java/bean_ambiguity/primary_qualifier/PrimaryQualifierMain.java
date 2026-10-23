package bean_ambiguity.primary_qualifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PrimaryQualifierMain {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(PrimaryQualifierMain.class, args);

        PrimaryQualifierOrderService orderService = context.getBean(PrimaryQualifierOrderService.class);

        orderService.placeOrder();
    }
}
