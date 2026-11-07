package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.*;
import com.dinaxis.ttbackend.model.dto.OutcomeRequestDTO;
import com.dinaxis.ttbackend.model.dto.TransactionResponseDTO;
import com.dinaxis.ttbackend.repository.MoneyTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock MoneyTransactionRepository moneyTransactionRepository;
    @InjectMocks TransactionService transactionService;

    private OutcomeRequestDTO outcome(TransactionCategory category, PaymentMethod method) {
        return new OutcomeRequestDTO(category, new BigDecimal("350.00"), method, "  Bolillos y chile  ");
    }

    @Test
    void outcomeIsSavedAsOutAndDefaultsToCash() {
        when(moneyTransactionRepository.save(any(MoneyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponseDTO res = transactionService.registerOutcome(outcome(TransactionCategory.SUPPLIES, null));

        ArgumentCaptor<MoneyTransaction> saved = ArgumentCaptor.forClass(MoneyTransaction.class);
        verify(moneyTransactionRepository).save(saved.capture());
        assertEquals(TransactionType.OUT, saved.getValue().getType());
        assertEquals(PaymentMethod.CASH, saved.getValue().getPaymentMethod());
        assertEquals("Bolillos y chile", saved.getValue().getDescription());
        assertNull(saved.getValue().getSubOrder());
        assertEquals(TransactionCategory.SUPPLIES, res.getCategory());
    }

    @Test
    void outcomeKeepsGivenPaymentMethod() {
        when(moneyTransactionRepository.save(any(MoneyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(PaymentMethod.TRANSFER, transactionService.registerOutcome(outcome(TransactionCategory.OTHER, PaymentMethod.TRANSFER)).getPaymentMethod());
    }

    @Test
    void saleCategoryIsRejectedAsOutcome() {
        assertThrows(HttpClientErrorException.class, () -> transactionService.registerOutcome(outcome(TransactionCategory.SALE, null)));
        verifyNoInteractions(moneyTransactionRepository);
    }

    @Test
    void listDefaultsToTodayCoveringTheWholeDay() {
        transactionService.getTransactions(null, null, null);

        ArgumentCaptor<Timestamp> from = ArgumentCaptor.forClass(Timestamp.class);
        ArgumentCaptor<Timestamp> to = ArgumentCaptor.forClass(Timestamp.class);
        verify(moneyTransactionRepository).findByCreateAtBetweenOrderByCreateAtDesc(from.capture(), to.capture());
        LocalDate today = LocalDate.now();
        assertEquals(today.atStartOfDay(), from.getValue().toLocalDateTime());
        assertEquals(today, to.getValue().toLocalDateTime().toLocalDate());
        assertTrue(to.getValue().toLocalDateTime().isAfter(today.atTime(23, 59, 59)));
    }

    @Test
    void listFiltersByTypeCaseInsensitive() {
        when(moneyTransactionRepository.findByTypeAndCreateAtBetweenOrderByCreateAtDesc(eq(TransactionType.OUT), any(), any())).thenReturn(List.of());
        assertTrue(transactionService.getTransactions(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 6), "out").isEmpty());
    }

    @Test
    void invalidListArgumentsAreRejected() {
        assertThrows(HttpClientErrorException.class, () -> transactionService.getTransactions(null, null, "SIDEWAYS"));
        assertThrows(HttpClientErrorException.class, () -> transactionService.getTransactions(LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 1), null));
    }
}
