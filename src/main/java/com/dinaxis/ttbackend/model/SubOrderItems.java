package com.dinaxis.ttbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "sub_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubOrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_order_id", nullable = false)
    @JsonIgnore
    private SubOrder subOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be non-negative and greater than zero")
    private Integer quantity;

    @Column(nullable = false, name = "unit_price")
    @Min(value = 1, message = "Unit price must be non-negative and greater than zero")
    private Double unitPrice;

    @Column(nullable = false, name = "total_price")
    @Min(value = 1, message = "Total price must be non-negative and greater than zero")
    private Double totalPrice;




}
