package com.dinaxis.ttbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    Double totalAmount;

    @Column(nullable = false, name = "create_at")
    Timestamp createAt = new Timestamp(System.currentTimeMillis());

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubOrder> subOrders;

    public void addSubOrder(SubOrder subOrder){
        subOrders.add(subOrder);
        subOrder.setOrder(this);
    }




}
