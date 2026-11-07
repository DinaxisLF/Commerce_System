package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.*;
import com.dinaxis.ttbackend.model.dto.OutcomeRequestDTO;
import com.dinaxis.ttbackend.model.dto.TransactionResponseDTO;
import com.dinaxis.ttbackend.repository.MoneyTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private MoneyTransactionRepository moneyTransactionRepository;

    // Money going out of the register. Sales are only created by completing a sub-order.
    public TransactionResponseDTO registerOutcome(OutcomeRequestDTO request){
        if(request.getCategory() == TransactionCategory.SALE){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SALE is not an outcome category, sales are recorded when a sub-order is completed");
        }
        MoneyTransaction outcome = new MoneyTransaction();
        outcome.setType(TransactionType.OUT);
        outcome.setCategory(request.getCategory());
        outcome.setAmount(request.getAmount());
        outcome.setPaymentMethod(request.getPaymentMethod() == null ? PaymentMethod.CASH : request.getPaymentMethod());
        outcome.setDescription(request.getDescription().trim());
        return toResponse(moneyTransactionRepository.save(outcome));
    }

    // Both dates inclusive; null means today. type is IN, OUT or null for both.
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactions(LocalDate from, LocalDate to, String type){
        LocalDate today = LocalDate.now();
        LocalDate start = from == null ? today : from;
        LocalDate end = to == null ? (from == null ? today : from) : to;
        if(start.isAfter(end)){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "'from' must be on or before 'to'");
        }
        Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(end.plusDays(1).atStartOfDay().minusNanos(1000));

        List<MoneyTransaction> transactions = type == null
                ? moneyTransactionRepository.findByCreateAtBetweenOrderByCreateAtDesc(startTs, endTs)
                : moneyTransactionRepository.findByTypeAndCreateAtBetweenOrderByCreateAtDesc(parseType(type), startTs, endTs);
        return transactions.stream().map(this::toResponse).toList();
    }

    private TransactionType parseType(String type){
        try{
            return TransactionType.valueOf(type.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid transaction type: " + type);
        }
    }

    private TransactionResponseDTO toResponse(MoneyTransaction t){
        return new TransactionResponseDTO(t.getId(), t.getType(), t.getCategory(), t.getAmount(), t.getPaymentMethod(),
                t.getDescription(), t.getCreateAt(), t.getSubOrder() == null ? null : t.getSubOrder().getId());
    }
}
