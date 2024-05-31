package com.example.amazonx.service;

import com.example.amazonx.component.GlobalState;
import com.example.amazonx.model.Order;
import com.example.amazonx.protobuf.AmazonUps.*;
import com.example.amazonx.protobuf.WorldAmazon;
import com.example.amazonx.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UPSHandler {

    private static final Logger logger = LoggerFactory.getLogger(UPSHandler.class);
    private OrderRepository orderRepository;
    private GlobalState globalState;

    @Autowired
    public UPSHandler(OrderRepository orderRepository
                      , GlobalState globalState
    ) {
        this.orderRepository = orderRepository;
        this.globalState = globalState;
    }

    @Async
    public void handleResponse(UAResponse response) {
        long seqnum = response.getAck();
        if (response.getType() == 0){
            globalState.removeUPSSeq(seqnum);
            handleArrived(response.getUaTruckArrive());
        } else if (response.getType() == 1){
            globalState.removeUPSSeq(seqnum);
            handleDelivered(response.getUaDelivered());
        } else {
            Order order = orderRepository.findById(globalState.getPackageId(seqnum));
            globalState.removeUPSSeq(seqnum);
            if (order != null) {
                order.setStatus("Error");
                orderRepository.save(order);
            }
        }
    }

    @Async
    public void handleArrived(UATruckArrive uaTruckArrive){
        Order order = orderRepository.findById(uaTruckArrive.getPackageid());
        if(order == null){
            logger.error("No mach order found for {}", uaTruckArrive);
            return;
        }
        order.setTruckid(uaTruckArrive.getTruckid());
        orderRepository.save(order);
    }

    @Async
    public void handleDelivered(UADelivered uaDelivered){
        Order order = orderRepository.findById(uaDelivered.getPackageid());
        if(order == null){
            logger.error("No mach order found for {}", uaDelivered);
            return;
        }
        order.setStatus("Delivered");
        order.setDestX(uaDelivered.getDestx());
        order.setDestY(uaDelivered.getDesty());
        orderRepository.save(order);
    }

}
