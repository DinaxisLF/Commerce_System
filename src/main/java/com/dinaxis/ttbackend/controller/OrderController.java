package com.dinaxis.ttbackend.controller;


import com.dinaxis.ttbackend.model.OrderStatus;
import com.dinaxis.ttbackend.model.dto.OrderRequestDTO;
import com.dinaxis.ttbackend.model.dto.OrderResponseDTO;
import com.dinaxis.ttbackend.model.dto.SubOrderDTO;
import com.dinaxis.ttbackend.model.dto.SubOrderResponseDTO;
import com.dinaxis.ttbackend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

     @Autowired
     private OrderService orderService;

     @PostMapping("/orders")
     public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO newOrder){

         OrderResponseDTO placeOrder = orderService.createOrder(newOrder);

         return new ResponseEntity<OrderResponseDTO>(placeOrder, HttpStatus.CREATED);

    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(){
         return new ResponseEntity<List<OrderResponseDTO>>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @GetMapping("/orders/{id:[0-9]+}")
    public ResponseEntity<OrderResponseDTO> getOrderSummary(@Valid @PathVariable int id){

        OrderResponseDTO orderSummary = orderService.getOrderSummary(id);

        return  new ResponseEntity<OrderResponseDTO>(orderSummary, HttpStatus.OK);
    }

    @GetMapping("/orders/status")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@Valid @RequestParam String status){

        if(!status.equalsIgnoreCase("PENDING") && !status.equalsIgnoreCase("COMPLETED") && !status.equalsIgnoreCase("CANCELED")){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<OrderResponseDTO> ordersByStatus = orderService.getOrdersByStatus(status);

        return new ResponseEntity<List<OrderResponseDTO>>(ordersByStatus, HttpStatus.OK);
    }


    @PatchMapping("orders/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@Valid @PathVariable int id, @RequestParam String status){

        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, status);

        return new ResponseEntity<OrderResponseDTO>(updatedOrder, HttpStatus.OK);
    }

}
