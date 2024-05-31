package com.example.upsx.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "trucks")
@NoArgsConstructor
@Getter
@Setter
public class TruckInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int x;
    private int y;

    public TruckInfo(int x, int y){
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