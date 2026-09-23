package com.dinaxis.ttbackend.repository;

import com.dinaxis.ttbackend.model.Order;
import com.dinaxis.ttbackend.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    public List<Order> findByStatus(OrderStatus status);

}
