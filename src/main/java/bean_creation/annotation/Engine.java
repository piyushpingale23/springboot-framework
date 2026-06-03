package bean_creation.annotation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

public class Engine {

    @Value("${engine.engineType}")
    private String engineType;

    @Value("${engine.engineName}")
    private String engineName;

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public Engine(String engineName, String engineType) {
        this.engineName = engineName;
        this.engineType = engineType;
    }

}


@Component
//@Primary
//@Qualifier("petrolEngine")
@Profile("petrolEngine")
class PetrolEngine extends Engine {
    PetrolEngine(@Value("${petrol.engineType}") String engineType,
                 @Value("${petrol.engineName}") String engineName) {
        super(engineName, engineType);
    }
}


@Component
//@Qualifier("dieselEngine")
@Profile("dieselEngine")
class DieselEngine extends Engine {
    DieselEngine(@Value("${diesel.engineType}") String engineType,
                 @Value("${diesel.engineName}") String engineName) {
        super(engineName, engineType);
    }
}