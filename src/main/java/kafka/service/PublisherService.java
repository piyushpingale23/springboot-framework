package kafka.service;

import org.springframework.kafka.support.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class PublisherService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUser(String message) {

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("user-events", message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Published user = [" + message + "] " +
                        "with offset = [" + result.getRecordMetadata().offset() + "] " +
                        "in topic = [" + result.getRecordMetadata().topic() + "] " +
                        "in partition = [" + result.getRecordMetadata().partition() + "]");
            } else {
                System.out.println("Unable to publish user : [" + ex.getMessage() + "]");
            }
        });
    }

    public void publishOrder(String message) {

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("order-events", message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Published order = [" + message + "] " +
                        "with offset = [" + result.getRecordMetadata().offset() + "] " +
                        "in topic = [" + result.getRecordMetadata().topic() + "] " +
                        "in partition = [" + result.getRecordMetadata().partition() + "]");
            } else {
                System.out.println("Unable to publish order : [" + ex.getMessage() + "]");
            }
        });
    }
}
