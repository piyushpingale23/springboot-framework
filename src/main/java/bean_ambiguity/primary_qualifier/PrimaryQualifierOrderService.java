package bean_ambiguity.primary_qualifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PrimaryQualifierOrderService {

    @Autowired
    @Qualifier("upiPaymentService")
    private PaymentService paymentService;

    public void placeOrder() {
        paymentService.pay();
    }
}
