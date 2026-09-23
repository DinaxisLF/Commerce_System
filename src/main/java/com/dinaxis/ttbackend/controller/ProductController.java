package com.dinaxis.ttbackend.controller;


import com.dinaxis.ttbackend.model.Product;
import com.dinaxis.ttbackend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product newProduct){

        Product createdProduct = productService.createProduct(newProduct);
        if(createdProduct == null){
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }else{
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(){
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id){{

        Product product = productService.getProductById(id);

        if(product != null){
            return new ResponseEntity<>(product, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }}

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product product){
        Product modifiedProduct = null;

        modifiedProduct = productService.updateProduct(id, product);

        if(modifiedProduct != null){
            return new ResponseEntity<>(modifiedProduct, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/products/stock/{id}/{stock}")
    public ResponseEntity<Product> updateProductStock(@PathVariable int id, @PathVariable int stock ) {

        if (stock < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            if (productService.getProductById(id) != null) {
                Product modifiedProduct = productService.updateProductStock(id, stock);
                return new ResponseEntity<>(modifiedProduct, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id){
        Product product = productService.getProductById(id);
        if(product != null){
            productService.deleteProduct(id);
            return new ResponseEntity<>("Product Deleted", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Product not found",HttpStatus.NOT_FOUND);
        }

    }






}
