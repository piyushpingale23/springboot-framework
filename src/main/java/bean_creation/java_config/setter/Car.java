package bean_creation.java_config.setter;

public class Car {

    private String carModel;
    private String carType;

    public Car() {
        System.out.println("Car Setter Called");
    }

    // Setter methods (Setter Injection)
    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public void startCar() {
        System.out.println("Car started with Model: "
                + carModel + " with Type: " + carType);
    }
}