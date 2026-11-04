package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.*;
import com.dinaxis.ttbackend.repository.OrderRepository;
import com.dinaxis.ttbackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
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
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
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
        orderService.updateSubOrderStatus(1, 1, "CANCELED");
        assertEquals(12, torta.getStock());
        assertEquals(0, new BigDecimal("85.00").compareTo(order.getTotalAmount()));
        assertEquals(OrderStatus.CANCELED, sub1.getStatus());
    }

    @Test
    void cancelingOrderRestoresStockOfNonCanceledSubOrdersOnly() {
        sub1.setStatus(OrderStatus.CANCELED); // its stock was already restored when it was canceled
        orderService.updateOrderStatus(1, "CANCELED");
        assertEquals(11, torta.getStock());
        assertEquals(OrderStatus.CANCELED, sub2.getStatus());
    }

    @Test
    void finalOrderCannotChange() {
        order.setStatus(OrderStatus.COMPLETED);
        assertThrows(HttpClientErrorException.class, () -> orderService.updateOrderStatus(1, "CANCELED"));
        assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 2, "COMPLETED"));
    }

    @Test
    void unknownSubOrderIsNotFound() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> orderService.updateSubOrderStatus(1, 99, "COMPLETED"));
        assertEquals(404, ex.getStatusCode().value());
    }
}
