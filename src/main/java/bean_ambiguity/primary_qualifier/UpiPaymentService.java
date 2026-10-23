package bean_ambiguity.primary_qualifier;

import org.springframework.stereotype.Service;

@Service
public class UpiPaymentService implements PaymentService {

    @Override
    public void pay() {
        System.out.println("Paid using UPI");
    }
}
