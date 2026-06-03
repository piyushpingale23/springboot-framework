package bean_creation.java_config.constructor;

public class Car {

    public String carModel;
    public String carType;

    // Constructor Injection
    public Car(String carModel, String carType) {
        this.carModel = carModel;
        this.carType = carType;
    }

    public void startCar() {
        System.out.println("Car started with Model: "
                + carModel + " with Type: " + carType);
    }
}