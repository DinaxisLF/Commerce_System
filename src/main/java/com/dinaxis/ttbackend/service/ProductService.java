package com.dinaxis.ttbackend.service;

import com.dinaxis.ttbackend.model.Product;
import com.dinaxis.ttbackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product newProduct){
        return productRepository.save(newProduct);
    }

    public Product getProductById(int id){
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public Product updateProduct(int id, Product product){

            return productRepository.findById(id).map(existingProduct -> {
                existingProduct.setName(product.getName());
                existingProduct.setCategory(product.getCategory());
                existingProduct.setPrice(product.getPrice());
                existingProduct.setStock(product.getStock());
                if (product.getActive() != null) {
                    existingProduct.setActive(product.getActive());
                }
                return productRepository.save(existingProduct);

            }).orElse(null);
    }

    public Product updateProductStock(int id, int stock){
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setStock(stock);
            return productRepository.save(existingProduct);
        }).orElse(null);
    }

    public boolean deleteProduct(int id){
        // Soft delete: sold products are referenced by sub_order_items, a hard delete would violate the FK.
        return productRepository.findById(id).map(product -> {
            product.setActive(false);
            productRepository.save(product);
            return true;
        }).orElse(false);
    }




}
