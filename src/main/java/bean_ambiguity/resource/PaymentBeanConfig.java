package bean_ambiguity.resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentBeanConfig {

    @Bean
    public PaymentService cashPaymentService() {
        return new CashPaymentService();
    }

    @Bean
    public PaymentService chequePaymentService() {
        return new ChequePaymentService();
    }
}
