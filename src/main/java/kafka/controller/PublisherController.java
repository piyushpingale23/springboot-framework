package kafka.controller;

import kafka.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publisher")
public class PublisherController {

    @Autowired
    PublisherService publisherService;

    // Publish users to the user-events topic
    @GetMapping("/publish-user")
    public void publishUser() {
        try {
            for (int i = 1; i <= 1000; i++) {
                publisherService.publishUser("user: " + i);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Publish orders to the order-events topic
    @GetMapping("/publish-order")
    public void publishOrder() {
        try {
            for (int i = 1; i <= 2000; i++) {
                publisherService.publishOrder("order: " + i);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
