package com.dinaxis.ttbackend.model;

import com.dinaxis.ttbackend.model.dto.SubOrderDTO;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    Double totalAmount = 0.0;

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
