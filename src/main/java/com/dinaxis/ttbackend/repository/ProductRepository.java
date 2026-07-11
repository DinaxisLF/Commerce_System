package com.dinaxis.ttbackend.repository;

import com.dinaxis.ttbackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
