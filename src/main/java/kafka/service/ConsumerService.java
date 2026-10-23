package kafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    // First consumer for the user-events topic
    @KafkaListener(topics = "user-events", groupId = "user-consumer-group")
    public void user_consumer_one(String message) {
        System.out.println(message);
    }

    // First consumer in the order-consumer-group
    @KafkaListener(topics = "order-events", groupId = "order-consumer-group")
    public void order_consumer_one(String message) {
        System.out.println(message);
    }

    // Second consumer in the order-consumer-group
    @KafkaListener(topics = "order-events", groupId = "order-consumer-group")
    public void order_consumer_two(String message) {
        System.out.println(message);
    }
}
