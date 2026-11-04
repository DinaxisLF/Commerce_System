package com.dinaxis.ttbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, name = "created_at")
    Timestamp createAt = new Timestamp(System.currentTimeMillis());

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubOrder> subOrders = new ArrayList<>();

    public void addSubOrder(SubOrder subOrder){
        subOrders.add(subOrder);
        subOrder.setOrder(this);
    }




}
