package com.example.upsx.repository;

import com.example.upsx.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findById(long id);
    List<Order> findByWhnum(int whnum);

    List<Order> findByPackageId(long packageid);
    List<Order> findByTruckIdAndStatus(int truckid, String status);
}
