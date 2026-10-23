package bean_ambiguity.primary_qualifier;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CreditCardPaymentService implements PaymentService {

    @Override
    public void pay() {
        System.out.println("Paid using Credit Card");
    }
}
