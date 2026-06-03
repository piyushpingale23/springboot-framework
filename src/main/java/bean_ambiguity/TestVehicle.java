package bean_ambiguity;

public class TestVehicle implements Vehicle {

    @Override
    public void move() {
        System.out.println("Test Vehicle is moving");
    }
}
