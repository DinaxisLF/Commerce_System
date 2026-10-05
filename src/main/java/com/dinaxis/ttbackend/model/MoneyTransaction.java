package com.dinaxis.ttbackend.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "money_transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MoneyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "transaction_type" )
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategory category;

    @Column(nullable = false)
    @Min(value = 1, message = "Amount must be non-negative and greater than zero")
    private Double amount;

    private String description;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createAt = LocalDateTime.now();



}
