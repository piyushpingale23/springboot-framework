package annotations.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class Main {
    public static void main(String[] args) {
        System.out.println("Main start!");
        SpringApplication.run(Main.class, args);
        System.out.println("Main end!");
    }
}
