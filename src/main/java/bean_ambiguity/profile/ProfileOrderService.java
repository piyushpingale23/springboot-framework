package bean_ambiguity.profile;

import org.springframework.stereotype.Service;

@Service
public class ProfileOrderService {

    private final NotificationService notificationService;

    public ProfileOrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void placeOrder() {
        notificationService.send("Order placed successfully");
    }
}
