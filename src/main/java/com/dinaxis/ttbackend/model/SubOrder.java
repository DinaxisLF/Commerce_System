package com.dinaxis.ttbackend.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sub_orders")
public class SubOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @Column(nullable = false, name = "customer_name")
    private String customerName;

    @Column(nullable = false, name = "subtotal_amount", precision = 10, scale = 2)
    private BigDecimal subtotalAmount;

    @OneToMany(mappedBy = "subOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubOrderItems> items = new ArrayList<>();

    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "amount_received", precision = 10, scale = 2)
    private BigDecimal amountReceived;

    public void addItem(SubOrderItems item) {
        items.add(item);
        item.setSubOrder(this);
    }


}
