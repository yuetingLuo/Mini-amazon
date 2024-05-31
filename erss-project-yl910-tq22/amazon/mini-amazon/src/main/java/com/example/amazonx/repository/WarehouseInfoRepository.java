package com.example.amazonx.repository;

import com.example.amazonx.model.Order;
import com.example.amazonx.model.WarehouseInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface WarehouseInfoRepository extends JpaRepository<WarehouseInfo, Integer> {
    WarehouseInfo findById(int id);

}
