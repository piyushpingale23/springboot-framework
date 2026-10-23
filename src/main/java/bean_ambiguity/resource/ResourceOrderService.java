package bean_ambiguity.resource;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceOrderService {

    @Autowired
    @Resource(name = "cashPaymentService")
    private PaymentService paymentService;

    public void placeOrder() {
        paymentService.pay();
    }
}
