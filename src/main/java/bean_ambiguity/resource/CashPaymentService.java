package bean_ambiguity.resource;

public class CashPaymentService implements PaymentService {

    @Override
    public void pay() {
        System.out.println("Paid using Cash");
    }
}
