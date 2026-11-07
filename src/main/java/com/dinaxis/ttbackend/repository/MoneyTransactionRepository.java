package com.dinaxis.ttbackend.repository;

import com.dinaxis.ttbackend.model.MoneyTransaction;
import com.dinaxis.ttbackend.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Integer> {

    List<MoneyTransaction> findByCreateAtBetweenOrderByCreateAtDesc(Timestamp from, Timestamp to);

    List<MoneyTransaction> findByTypeAndCreateAtBetweenOrderByCreateAtDesc(TransactionType type, Timestamp from, Timestamp to);
}
