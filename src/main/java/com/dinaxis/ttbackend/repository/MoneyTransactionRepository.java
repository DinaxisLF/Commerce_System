package com.dinaxis.ttbackend.repository;

import com.dinaxis.ttbackend.model.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Integer> {
}
