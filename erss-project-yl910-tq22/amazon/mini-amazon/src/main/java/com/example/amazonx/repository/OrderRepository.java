package com.example.amazonx.repository;

import com.example.amazonx.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findById(long id);
    List<Order> findByWhnumAndProductId(int whnum, long product_id);

    @Query("SELECT o FROM Order o WHERE o.user.username = :username")
    List<Order> findAllByUserUsername(@Param("username") String username);
}
