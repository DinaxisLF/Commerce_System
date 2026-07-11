package com.dinaxis.ttbackend.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, name = "customer_name")
    private String customerName;

    @Column(nullable = false, name = "subtotal_amount")
    @Min(value = 1, message = "Subtotal amount must be non-negative and greater than zero")
    private Double subtotalAmount;

    @OneToMany(mappedBy = "subOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubOrderItems> items = new ArrayList<>();

    public void addItem(SubOrderItems item) {
        items.add(item);
        item.setSubOrder(this);
    }


}
