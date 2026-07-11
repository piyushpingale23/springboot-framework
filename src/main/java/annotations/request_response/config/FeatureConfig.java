package annotations.request_response.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureConfig {

    @Bean
    @ConditionalOnProperty(
            name = "feature.logging.enabled",
            havingValue = "true"
    )
    public String loggingFeature() {
        return "Logging Feature is ENABLED";
    }
}