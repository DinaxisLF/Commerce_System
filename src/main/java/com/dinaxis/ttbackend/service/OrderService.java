package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.Order;
import com.dinaxis.ttbackend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order newOrder) {

        return orderRepository.save(newOrder);
    }

}
