package bean_scope.singleton;

public class Car {

    public String carModel;
    public String carType;

    public Car(String carModel, String carType) {
        this.carModel = carModel;
        this.carType = carType;
    }

    public Car() {

    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public void startCar() {
        System.out.println("Car started with Model: " + carModel + " with Type: " + carType);
    }
}
