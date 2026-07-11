package com.dinaxis.ttbackend.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.Transaction;

import java.sql.Timestamp;

@Entity
@Table(name = "money_transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MoneyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, name = "transaction_type" )
    private TransactionType type;

    @Column(nullable = false)
    private TransactionCategory category;

    @Column(nullable = false)
    @Min(value = 1, message = "Amount must be non-negative and greater than zero")
    private Double amount;

    private String description;

    @Column(nullable = false, name = "created_at")
    private Timestamp createAt = new Timestamp(System.currentTimeMillis());



}
