package com.example.amazonx.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name = "products")
@NoArgsConstructor
@Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String base64;

    public Product(String description, String img) {
        this.description = description;
        this.base64 = base64;
    }
}