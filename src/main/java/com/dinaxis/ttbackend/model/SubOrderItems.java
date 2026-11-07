package com.dinaxis.ttbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sub_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubOrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_order_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SubOrder subOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be non-negative and greater than zero")
    private Integer quantity;

    @Column(nullable = false, name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private String notes;




}
