package bean_ambiguity.resource;

public class ChequePaymentService implements PaymentService {

    @Override
    public void pay() {
        System.out.println("Paid using Cheque");
    }
}
