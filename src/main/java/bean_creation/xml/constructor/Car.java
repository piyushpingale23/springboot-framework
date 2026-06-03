package bean_creation.xml.constructor;

public class Car {

    public String carModel;
    public String carType;

    public Car(String carModel, String carType) {
        this.carModel = carModel;
        this.carType = carType;
    }

    public Car() {

    }

    public void startCar() {
        System.out.println("Car started with Model: " + carModel + " with Type: " + carType);
    }
}
