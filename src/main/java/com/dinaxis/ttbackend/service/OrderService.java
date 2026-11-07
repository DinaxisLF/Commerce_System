package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.*;
import com.dinaxis.ttbackend.model.dto.*;
import com.dinaxis.ttbackend.repository.MoneyTransactionRepository;
import com.dinaxis.ttbackend.repository.OrderRepository;
import com.dinaxis.ttbackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private MoneyTransactionRepository moneyTransactionRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO newOrder) {
        Order order = new Order();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for(SubOrderDTO subOrder : newOrder.getSubOrders()){
            SubOrder newSubOrder = new SubOrder();
            newSubOrder.setCustomerName(subOrder.getCustomerName());
            BigDecimal subOrderTotal = BigDecimal.ZERO;

            for(OrderItemRequestDTO item : subOrder.getItems()){
                Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Product not found with id: " + item.getProductId())) ;

                if(!product.getActive()){
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,"Product not available: " + product.getName());
                }
                // Stock is checked and decremented in the database, so a concurrent order can't oversell. Throwing rolls back earlier items.
                if(productRepository.decrementStock(product.getId(), item.getQuantity()) == 0){
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Product out of stock: " + product.getName());
                }

                SubOrderItems itemSub = new SubOrderItems();
                itemSub.setQuantity(item.getQuantity());
                itemSub.setUnitPrice(product.getPrice());
                itemSub.setProduct(product);
                itemSub.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                itemSub.setNotes(item.getNotes());
                newSubOrder.addItem(itemSub);
                subOrderTotal = subOrderTotal.add(itemSub.getTotalPrice());
            }
            newSubOrder.setSubtotalAmount(subOrderTotal);
            order.addSubOrder(newSubOrder);
            grandTotal = grandTotal.add(subOrderTotal);
        }

        order.setTotalAmount(grandTotal);
        orderRepository.save(order);
        return toResponse(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<OrderResponseDTO> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(parseStatus(status)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderSummaryDTO getOrderDetail(int orderId){
        Order order = findOrder(orderId);
        OrderSummaryDTO detail = new OrderSummaryDTO();
        detail.setOrderId(order.getId());
        detail.setTotalAmount(order.getTotalAmount());
        detail.setStatus(order.getStatus());
        detail.setCreatedAt(order.getCreateAt());
        detail.setSubOrders(order.getSubOrders().stream().map(this::toSubOrderResponse).toList());
        return detail;
    }

    // PENDING -> COMPLETED (all non-canceled sub-orders done) or PENDING -> CANCELED (restores stock).
    @Transactional
    public OrderResponseDTO updateOrderStatus(int orderId, String status){
        Order order = findOrder(orderId);
        OrderStatus newStatus = parseStatus(status);
        requirePending(order.getStatus(), "Order");

        if(newStatus == OrderStatus.COMPLETED){
            List<SubOrder> active = order.getSubOrders().stream().filter(s -> s.getStatus() != OrderStatus.CANCELED).toList();
            if(active.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Order has no active sub-orders, cancel it instead");
            }
            if(active.stream().anyMatch(s -> s.getStatus() == OrderStatus.PENDING)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "All sub-orders must be completed before completing the order");
            }
        }else if(newStatus == OrderStatus.CANCELED){
            if(order.getSubOrders().stream().anyMatch(s -> s.getStatus() == OrderStatus.COMPLETED)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Order has paid sub-orders, cancel the pending sub-orders and complete the order instead");
            }
            for(SubOrder subOrder : order.getSubOrders()){
                if(subOrder.getStatus() != OrderStatus.CANCELED){
                    restoreStock(subOrder);
                    subOrder.setStatus(OrderStatus.CANCELED);
                }
            }
        }else{
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Order is already PENDING");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        return toResponse(order);
    }

    // PENDING -> COMPLETED (takes the payment, records the sale), or PENDING -> CANCELED (restores stock, subtracts the subtotal from the order total).
    @Transactional
    public SubOrderResponseDTO updateSubOrderStatus(int orderId, int subOrderId, SubOrderStatusRequestDTO request){
        Order order = findOrder(orderId);
        SubOrder subOrder = order.getSubOrders().stream().filter(s -> s.getId() == subOrderId).findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Sub-order " + subOrderId + " not found in order " + orderId));
        OrderStatus newStatus = parseStatus(request.getStatus());
        requirePending(order.getStatus(), "Order");
        requirePending(subOrder.getStatus(), "Sub-order");

        if(newStatus == OrderStatus.PENDING){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Sub-order is already PENDING");
        }
        if(newStatus == OrderStatus.COMPLETED){
            takePayment(subOrder, request);
        }else if(newStatus == OrderStatus.CANCELED){
            restoreStock(subOrder);
            order.setTotalAmount(order.getTotalAmount().subtract(subOrder.getSubtotalAmount()));
        }

        subOrder.setStatus(newStatus);
        orderRepository.save(order);
        return toSubOrderResponse(subOrder);
    }

    private void takePayment(SubOrder subOrder, SubOrderStatusRequestDTO request){
        PaymentMethod method = request.getPaymentMethod();
        if(method == null){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Payment method is required to complete a sub-order");
        }
        BigDecimal received = null;
        if(method == PaymentMethod.CASH){
            received = request.getAmountReceived();
            if(received == null || received.compareTo(subOrder.getSubtotalAmount()) < 0){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Cash received must cover the subtotal of " + subOrder.getSubtotalAmount());
            }
        }
        subOrder.setPaymentMethod(method);
        subOrder.setAmountReceived(received);

        MoneyTransaction sale = new MoneyTransaction();
        sale.setType(TransactionType.IN);
        sale.setCategory(TransactionCategory.SALE);
        sale.setAmount(subOrder.getSubtotalAmount());
        sale.setPaymentMethod(method);
        sale.setSubOrder(subOrder);
        sale.setDescription("Sale: order " + subOrder.getOrder().getId() + ", sub-order " + subOrder.getId());
        moneyTransactionRepository.save(sale);
    }

    private Order findOrder(int orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
    }

    private OrderStatus parseStatus(String status){
        try{
            return OrderStatus.valueOf(status.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private void requirePending(OrderStatus current, String what){
        if(current != OrderStatus.PENDING){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, what + " is already " + current + " and cannot be changed");
        }
    }

    private void restoreStock(SubOrder subOrder){
        for(SubOrderItems item : subOrder.getItems()){
            productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private OrderResponseDTO toResponse(Order order){
        return new OrderResponseDTO(order.getId(), order.getTotalAmount(), order.getStatus(), order.getCreateAt());
    }

    private SubOrderResponseDTO toSubOrderResponse(SubOrder subOrder){
        SubOrderResponseDTO dto = new SubOrderResponseDTO();
        dto.setOrderId(subOrder.getOrder().getId());
        dto.setSubOrderId(subOrder.getId());
        dto.setCustomerName(subOrder.getCustomerName());
        dto.setSubtotalAmount(subOrder.getSubtotalAmount());
        dto.setStatus(subOrder.getStatus());
        dto.setPaymentMethod(subOrder.getPaymentMethod());
        dto.setAmountReceived(subOrder.getAmountReceived());
        if(subOrder.getAmountReceived() != null){
            dto.setChange(subOrder.getAmountReceived().subtract(subOrder.getSubtotalAmount()));
        }
        dto.setItems(subOrder.getItems().stream().map(item -> new OrderItemResponseDTO(
                item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(),
                item.getUnitPrice(), item.getTotalPrice(), item.getNotes())).toList());
        return dto;
    }
}
