package graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GraphqlMain {

    public static void main(String[] args) {
        SpringApplication.run(graphql.GraphqlMain.class, args);
    }

}
