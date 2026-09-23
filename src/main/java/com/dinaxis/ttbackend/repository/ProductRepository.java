package com.dinaxis.ttbackend.repository;

import com.dinaxis.ttbackend.model.OrderStatus;
import com.dinaxis.ttbackend.model.Product;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    public Integer getStockById(int id);

    public OrderStatus getStatusById(int id);

    public Double getPriceById(int id);
}
