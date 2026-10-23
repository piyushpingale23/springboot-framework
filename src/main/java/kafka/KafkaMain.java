package kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KafkaMain {

    public static void main(String[] args) {
        SpringApplication.run(kafka.KafkaMain.class, args);
    }

}
