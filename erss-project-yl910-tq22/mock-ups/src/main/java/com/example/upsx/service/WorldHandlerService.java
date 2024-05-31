package com.example.upsx.service;


import com.example.upsx.protobuf.WorldUps.*;
import com.example.upsx.repository.OrderRepository;
import com.example.upsx.model.Order;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Service
public class WorldHandlerService {

    private OrderRepository orderRepository;

    public WorldHandlerService(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    @Async
    public void handleResponse(UResponses response) {
        response.getCompletionsList().forEach(this::onCompletion);
        response.getDeliveredList().forEach(this::onDelivered);
    }

    @Async
    public void onCompletion(UFinished completed) {
        System.out.println("completed:" + completed);

        // find matched order from model
        List<Order> orders = orderRepository.findByTruckIdAndStatus(completed.getTruckid(), "Picking");
        if (orders.isEmpty()) {
            return;
        }
        Order order = orders.get(0);
        order.setStatus("Arrived");
        orderRepository.save(order);
    }

    @Async
    public void onDelivered(UDeliveryMade delivered) {
        System.out.println("delivered:" + delivered);

        // find matched order from model
        List<Order> orders = orderRepository.findByTruckIdAndStatus(delivered.getTruckid(), "Delivering");
        if (orders.isEmpty()) {
            return;
        }
        Order order = orders.get(0);
        order.setStatus("Delivered");
        orderRepository.save(order);
    }
}
