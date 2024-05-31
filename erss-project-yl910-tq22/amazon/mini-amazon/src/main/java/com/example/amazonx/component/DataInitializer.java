package com.example.amazonx.component;

import com.example.amazonx.model.Product;
import com.example.amazonx.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer.run() called");
        // Check if the repository is empty and only then load data
        if (productRepository.count() == 0) {
            loadProductData();
        }
    }

    public void loadProductData() {
        try {
            File file = new File("/app/data/products.json");
            ObjectMapper mapper = new ObjectMapper();
            List<Product> productList = mapper.readValue(file, new TypeReference<List<Product>>() {});
            productRepository.saveAll(productList);
        } catch (Exception e) {
            logger.error("Error when parse json {}", e.getMessage());
        }
    }
}

class Data {
     String description;
     String img;
}
