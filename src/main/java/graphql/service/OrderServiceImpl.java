package graphql.service;

import graphql.entity.Order;
import graphql.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById (Integer orderId) {
        return orderRepository.findById(orderId).get();
    }

    public Boolean deleteOrderById(Integer orderId) {
        orderRepository.deleteById(orderId);
        return true;
    }

}
