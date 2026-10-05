package com.dinaxis.ttbackend.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(nullable = false)
    @NotNull
    private String name;
    @Enumerated(EnumType.STRING)
    @NotNull
    private Category category;
    @Column(nullable = false)
    @Min(value = 1, message = "Price must be non-negative and greater than zero")
    private Double price;
    @Column(nullable = false)
    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;
    @Column(name = "is_active")
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
    }

}
