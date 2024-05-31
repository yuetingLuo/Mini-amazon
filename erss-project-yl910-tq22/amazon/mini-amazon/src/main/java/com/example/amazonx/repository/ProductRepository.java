package com.example.amazonx.repository;

import com.example.amazonx.model.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDescriptionContainingIgnoreCase(String name);
    List<Product> findById(long id);;

//    List<Product>
}