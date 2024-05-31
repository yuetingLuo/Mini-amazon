package com.example.upsx.model;

import javax.persistence.*;


@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private final String description;

    public Product(String description) {
        this.description = description;
    }

    protected Product() {
        this.description = ""; // Assign a default value
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}