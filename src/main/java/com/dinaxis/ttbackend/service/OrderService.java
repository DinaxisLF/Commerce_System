package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.*;
import com.dinaxis.ttbackend.model.dto.*;
import com.dinaxis.ttbackend.repository.OrderRepository;
import com.dinaxis.ttbackend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO newOrder) {
        Order order = new Order();
        Double grandTotal = 0.0;

        for(SubOrderDTO subOrder : newOrder.getSubOrders()){
            SubOrder newSubOrder = new SubOrder();
            newSubOrder.setCustomerName(subOrder.getCustomerName());
            Double subOrderTotal = 0.0;

            for(OrderItemRequestDTO item : subOrder.getItems()){
                Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Product not found with id: " + item.getProductId())) ;
                if(product.getStock() < item.getQuantity()){
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Product out of stock: " + product.getName());
                }else if(!product.getActive()){
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,"Product not available: " + product.getName());
                }
                SubOrderItems itemSub = new SubOrderItems();
                itemSub.setQuantity(item.getQuantity());
                itemSub.setUnitPrice(product.getPrice());
                itemSub.setProduct(product);
                itemSub.setTotalPrice(product.getPrice() * item.getQuantity());
                newSubOrder.addItem(itemSub);
                subOrderTotal += itemSub.getTotalPrice();

                product.setStock(product.getStock() -item.getQuantity());
                productRepository.save(product);
            }

            newSubOrder.setSubtotalAmount(subOrderTotal);
            order.addSubOrder(newSubOrder);
            grandTotal += subOrderTotal;

        }
        order.setTotalAmount(grandTotal);
        orderRepository.save(order);
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setOrderId(order.getId());
        orderResponseDTO.setTotalAmount(order.getTotalAmount());
        orderResponseDTO.setCreatedAt(order.getCreateAt());
        orderResponseDTO.setStatus(order.getStatus());

        return orderResponseDTO;

    }

    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDTO> orderResponseDTOList = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
            orderResponseDTO.setOrderId(order.getId());
            orderResponseDTO.setStatus(order.getStatus());
            orderResponseDTO.setCreatedAt(order.getCreateAt());
            orderResponseDTO.setTotalAmount(order.getTotalAmount());
            orderResponseDTOList.add(orderResponseDTO);
        }

        return orderResponseDTOList;
    }

    public List<OrderResponseDTO> getOrdersByStatus(String status) {
        OrderStatus orderStatus;
        try{
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid order status: " + status);
        }

        List<Order> orders = orderRepository.findByStatus(orderStatus);
        List<OrderResponseDTO> orderResponseDTOList = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
            orderResponseDTO.setOrderId(order.getId());
            orderResponseDTO.setStatus(order.getStatus());
            orderResponseDTO.setCreatedAt(order.getCreateAt());
            orderResponseDTO.setTotalAmount(order.getTotalAmount());
            orderResponseDTOList.add(orderResponseDTO);
        }

        return orderResponseDTOList;
    }

    public OrderResponseDTO getOrderSummary(int orderId){

        OrderResponseDTO orderSummaryDTO  = new OrderResponseDTO();

        Optional<Order> order = orderRepository.findById(orderId);

        if(order.isEmpty()) return null;

        orderSummaryDTO.setOrderId(order.get().getId());
        orderSummaryDTO.setStatus(order.get().getStatus());
        orderSummaryDTO.setCreatedAt(order.get().getCreateAt());
        orderSummaryDTO.setTotalAmount(order.get().getTotalAmount());

        return orderSummaryDTO;


    }

    public OrderResponseDTO updateOrderStatus(int orderId, String status){

        Optional<Order> order = orderRepository.findById(orderId);

        if(order.isEmpty()) return null;

        OrderStatus newStatus;
        try{
            newStatus = OrderStatus.valueOf(status.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid order status: " + status);
        }

        order.get().setStatus(newStatus);
        orderRepository.save(order.get());

        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setOrderId(order.get().getId());
        orderResponseDTO.setStatus(order.get().getStatus());
        orderResponseDTO.setCreatedAt(order.get().getCreateAt());
        orderResponseDTO.setTotalAmount(order.get().getTotalAmount());

        return orderResponseDTO;
    }

    public SubOrderResponseDTO updateSubOrderStatus(int orderId, int subOrderId, String status){

        Optional<Order> order = orderRepository.findById(orderId);

        if(order.isEmpty()) return null;

        SubOrder subOrder = order.get().getSubOrders().stream().filter(s -> s.getId() == subOrderId).findFirst().orElse(null);

        if(subOrder == null) return null;

        OrderStatus newStatus;
        try{
            newStatus = OrderStatus.valueOf(status.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid sub-order status: " + status);
        }

        subOrder.setStatus(newStatus);
        orderRepository.save(order.get());

        SubOrderResponseDTO subOrderResponseDTO = new SubOrderResponseDTO();
        subOrderResponseDTO.setSubOrderId(subOrder.getId());
        subOrderResponseDTO.setStatus(subOrder.getStatus());
        subOrderResponseDTO.setItems(subOrder.getItems().stream().map(item -> {
            OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setQuantity(item.getQuantity());
            return itemDTO;
        }).toList());

        return subOrderResponseDTO;
    }

}
