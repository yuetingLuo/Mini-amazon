package com.example.amazonx.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "warehouses")
@NoArgsConstructor
@Getter
@Setter
public class WarehouseInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int x;
    private int y;

    public WarehouseInfo(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}