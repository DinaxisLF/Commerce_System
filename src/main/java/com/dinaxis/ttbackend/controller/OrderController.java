package com.dinaxis.ttbackend.controller;


import com.dinaxis.ttbackend.model.Order;
import com.dinaxis.ttbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

     @Autowired
     private OrderService orderService;

     @PostMapping("/order")
     public ResponseEntity<Order> createOrder(@RequestBody Order newOrder){
        Order createdOrder = orderService.createOrder(newOrder);
        if(createdOrder == null){
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }else{
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        }
    }

    @GetMapping("/order")
    public ResponseEntity<Order>
}
