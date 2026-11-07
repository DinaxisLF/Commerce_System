package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.*;
import com.dinaxis.ttbackend.model.dto.*;
import com.dinaxis.ttbackend.repository.MoneyTransactionRepository;
import com.dinaxis.ttbackend.repository.OrderRepository;
import com.dinaxis.ttbackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock MoneyTransactionRepository moneyTransactionRepository;
    @InjectMocks OrderService orderService;

    Product torta;
    Order order;
    SubOrder sub1, sub2;

    @BeforeEach
    void setUp() {
        torta = new Product(1, "Torta ahogada", Category.TORTA, new BigDecimal("85.00"), 10, true);
        order = new Order();
        order.setId(1);
        sub1 = subOrder(1, 2);
        sub2 = subOrder(2, 1);
        order.addSubOrder(sub1);
        order.addSubOrder(sub2);
        order.setTotalAmount(new BigDecimal("255.00"));
        lenient().when(orderRepository.findById(1)).thenReturn(Optional.of(order)); // createOrder tests don't look orders up
    }

    private SubOrder subOrder(int id, int qty) {
        SubOrder s = new SubOrder();
        s.setId(id);
        s.setCustomerName("Cliente " + id);
        SubOrderItems item = new SubOrderItems();
        item.setProduct(torta);
        item.setQuantity(qty);
        item.setUnitPrice(torta.getPrice());
        item.setTotalPrice(torta.getPrice().multiply(BigDecimal.valueOf(qty)));
        s.addItem(item);
        s.setSubtotalAmount(item.getTotalPrice());
        return s;
    }

    @Test
    void cannotCompleteOrderWithPendingSubOrders() {
        sub1.setStatus(OrderStatus.COMPLETED);
        assertThrows(HttpClientErrorException.class, () -> orderService.updateOrderStatus(1, "completed"));
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void completesOrderWhenEveryActiveSubOrderIsDone() {
        sub1.setStatus(OrderStatus.COMPLETED);
        sub2.setStatus(OrderStatus.CANCELED);
        assertEquals(OrderStatus.COMPLETED, orderService.updateOrderStatus(1, "COMPLETED").getStatus());
    }

    @Test
    void cannotCompleteOrderWhenAllSubOrdersCanceled() {
        sub1.setStatus(OrderStatus.CANCELED);
        sub2.setStatus(OrderStatus.CANCELED);
        assertThrows(HttpClientErrorException.class, () -> orderService.updateOrderStatus(1, "COMPLETED"));
    }

    @Test
    void cancelingSubOrderRestoresStockAndLowersTotal() {
        orderService.updateSubOrderStatus(1, 1, req("CANCELED", null, null));
        verify(productRepository).incrementStock(1, 2);
        assertEquals(0, new BigDecimal("85.00").compareTo(order.getTotalAmount()));
        assertEquals(OrderStatus.CANCELED, sub1.getStatus());
    }

    @Test
    void cancelingOrderRestoresStockOfNonCanceledSubOrdersOnly() {
        sub1.setStatus(OrderStatus.CANCELED); // its stock was already restored when it was canceled
        orderService.updateOrderStatus(1, "CANCELED");
        verify(productRepository).incrementStock(1, 1); // sub2 only
        verify(productRepository, never()).incrementStock(1, 2);
        assertEquals(OrderStatus.CANCELED, sub2.getStatus());
    }

    @Test
    void finalOrderCannotChange() {
        order.setStatus(OrderStatus.COMPLETED);
        assertThrows(HttpClientErrorException.class, () -> orderService.updateOrderStatus(1, "CANCELED"));
        assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 2, req("COMPLETED", PaymentMethod.CARD, null)));
    }

    @Test
    void unknownSubOrderIsNotFound() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 99, req("COMPLETED", PaymentMethod.CARD, null)));
        assertEquals(404, ex.getStatusCode().value());
    }

    // --- payments ---

    private SubOrderStatusRequestDTO req(String status, PaymentMethod method, String received) {
        return new SubOrderStatusRequestDTO(status, method, received == null ? null : new BigDecimal(received));
    }

    @Test
    void completingSubOrderRequiresPaymentMethod() {
        assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 1, req("COMPLETED", null, null)));
        assertEquals(OrderStatus.PENDING, sub1.getStatus());
        verifyNoInteractions(moneyTransactionRepository);
    }

    @Test
    void cashBelowSubtotalIsRejected() {
        // sub1 subtotal is 170.00
        assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 1, req("COMPLETED", PaymentMethod.CASH, "169.99")));
        assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 1, req("COMPLETED", PaymentMethod.CASH, null)));
        verifyNoInteractions(moneyTransactionRepository);
    }

    @Test
    void cashPaymentRecordsSaleAndReturnsChange() {
        SubOrderResponseDTO res = orderService.updateSubOrderStatus(1, 1, req("COMPLETED", PaymentMethod.CASH, "200"));

        ArgumentCaptor<MoneyTransaction> sale = ArgumentCaptor.forClass(MoneyTransaction.class);
        verify(moneyTransactionRepository).save(sale.capture());
        assertEquals(TransactionType.IN, sale.getValue().getType());
        assertEquals(TransactionCategory.SALE, sale.getValue().getCategory());
        assertEquals(PaymentMethod.CASH, sale.getValue().getPaymentMethod());
        assertEquals(0, new BigDecimal("170.00").compareTo(sale.getValue().getAmount()));
        assertSame(sub1, sale.getValue().getSubOrder());

        assertEquals(OrderStatus.COMPLETED, res.getStatus());
        assertEquals(0, new BigDecimal("30.00").compareTo(res.getChange()));
    }

    @Test
    void cardPaymentIgnoresAmountReceived() {
        SubOrderResponseDTO res = orderService.updateSubOrderStatus(1, 2, req("COMPLETED", PaymentMethod.CARD, "500"));
        assertNull(res.getAmountReceived());
        assertNull(res.getChange());
        verify(moneyTransactionRepository).save(any(MoneyTransaction.class));
    }

    @Test
    void orderWithPaidSubOrderCannotBeCanceled() {
        sub1.setStatus(OrderStatus.COMPLETED);
        assertThrows(HttpClientErrorException.class, () -> orderService.updateOrderStatus(1, "CANCELED"));
        verify(productRepository, never()).incrementStock(anyInt(), anyInt());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    // --- stock concurrency ---

    private OrderRequestDTO orderOf(int productId, int qty) {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(productId);
        item.setQuantity(qty);
        SubOrderDTO sub = new SubOrderDTO();
        sub.setCustomerName("Sara");
        sub.setItems(List.of(item));
        OrderRequestDTO request = new OrderRequestDTO();
        request.setSubOrders(List.of(sub));
        return request;
    }

    @Test
    void createOrderDecrementsStockAtomically() {
        when(productRepository.findById(1)).thenReturn(Optional.of(torta));
        when(productRepository.decrementStock(1, 3)).thenReturn(1);

        OrderResponseDTO res = orderService.createOrder(orderOf(1, 3));

        verify(productRepository).decrementStock(1, 3);
        verify(productRepository, never()).save(any());
        assertEquals(0, new BigDecimal("255.00").compareTo(res.getTotalAmount()));
    }

    @Test
    void createOrderFailsWhenAnotherOrderTookTheStock() {
        // In-memory stock says 10, but the database update matched no row: a concurrent order got there first.
        when(productRepository.findById(1)).thenReturn(Optional.of(torta));
        when(productRepository.decrementStock(1, 3)).thenReturn(0);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> orderService.createOrder(orderOf(1, 3)));
        assertEquals(400, ex.getStatusCode().value());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void inactiveProductIsRejectedBeforeTouchingStock() {
        torta.setActive(false);
        when(productRepository.findById(1)).thenReturn(Optional.of(torta));
        assertThrows(HttpClientErrorException.class, () -> orderService.createOrder(orderOf(1, 1)));
        verify(productRepository, never()).decrementStock(anyInt(), anyInt());
    }
}
