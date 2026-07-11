package annotations.request_response.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/*
 * @Configuration
 * --------------
 * Marks this class as a Spring Configuration class.
 *
 * Purpose:
 * - Allows Spring to discover and process this class during startup.
 * - Commonly used to define @Bean methods.
 * - Used for application configuration.
 *
 * Internally:
 * @Configuration is a specialized form of @Component,
 * so Spring creates a bean for this class.
 *
 * ----------------------------------------------------
 *
 * @PropertySource
 * ---------------
 * Loads an external properties file into the Spring Environment.
 *
 * File:
 * application-custom.properties
 *
 * Example:
 * app.name=Spring Boot Framework
 * app.version=1.0
 *
 * Once loaded, properties can be accessed using: @Value("${app.name}") and @Value("${app.version}")
 *
 * @Configuration
 * -> Used to define Spring configuration classes.
 *
 * @PropertySource
 * -> Loads an external .properties file into the Spring Environment.
 */
@Configuration
@PropertySource("classpath:application-custom.properties")
public class AppConfig {
}