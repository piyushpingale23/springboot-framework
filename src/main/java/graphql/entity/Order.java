package graphql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    private String orderDetails;
    private String orderAddress;
    private String orderPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
