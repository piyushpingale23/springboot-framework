package bean_scope.prototype;

public class Bike {

    public String bikeModel;
    public String bikeType;

    public Bike(String bikeModel, String bikeType) {
        this.bikeModel = bikeModel;
        this.bikeType = bikeType;
    }

    public String getBikeModel() {
        return bikeModel;
    }

    public void setBikeModel(String bikeModel) {
        this.bikeModel = bikeModel;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public Bike(){

    }

    public void startCar() {
        System.out.println("Bike started with Model: " + bikeModel + " with Type: " + bikeType);
    }
}
