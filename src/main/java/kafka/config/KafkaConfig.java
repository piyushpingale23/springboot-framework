package kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic createUserTopic() {
        return new NewTopic("user-events", 1, (short) 1);
    }

    @Bean
    public NewTopic createOrderTopic() {
        return new NewTopic("order-events", 2, (short) 1);
    }

    @Bean
    public NewTopic createPaymentTopic() {
        return new NewTopic("payment-events", 3, (short) 1);
    }

    @Bean
    public NewTopic createTransactionTopic() {
        return new NewTopic("transaction-events", 4, (short) 1);
    }

}
