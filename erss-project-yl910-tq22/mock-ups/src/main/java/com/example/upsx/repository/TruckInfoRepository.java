package com.example.upsx.repository;

import com.example.upsx.model.TruckInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckInfoRepository extends JpaRepository<TruckInfo, Long> {
}
