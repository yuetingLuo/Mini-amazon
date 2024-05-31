package com.example.amazonx.service;

import com.example.amazonx.component.GlobalState;
import com.example.amazonx.protobuf.AmazonUps.*;
import com.example.amazonx.protobuf.WorldAmazon.*;

import com.example.amazonx.repository.OrderRepository;
import com.example.amazonx.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class WorldHandler {

    private static final Logger logger = LoggerFactory.getLogger(WorldHandler.class);
    private GlobalState globalState;
    private OrderRepository orderRepository;
    private WorldProtoGen worldProtoGen;
    private WorldSocketService worldSocketService;
    private UPSProtoGen upsProtoGen;
    private UPSSocketService upsSocketService;


    @Autowired
    public WorldHandler(GlobalState globalState
            , OrderRepository orderRepository
            , WorldProtoGen worldProtoGen
            , @Lazy WorldSocketService worldSocketService
            , UPSProtoGen upsProtoGen
            , UPSSocketService upsSocketService
    ) {
        this.globalState = globalState;
        this.orderRepository = orderRepository;
        this.worldProtoGen = worldProtoGen;
        this.worldSocketService = worldSocketService;
        this.upsProtoGen = upsProtoGen;
        this.upsSocketService = upsSocketService;
    }

    //handler 收到response遍历然后调各个接口
    @Async
    public void handleResponse(AResponses response) {

//        response.getAcksList().forEach(ack -> {
//            logger.info("Received ack: " + ack.toString());
//            globalState.removeBeAcked(ack);
//        });

        response.getAcksList().forEach(globalState::removeBeAcked);
        response.getArrivedList().forEach(this::onArrived);
        response.getReadyList().forEach(this::onReady);
        response.getLoadedList().forEach(this::onLoaded);
        response.getErrorList().forEach(this::onError);
    }

    @Async
    public void onArrived(APurchaseMore arrived) {
        logger.info("onArrived: " + arrived);
        // find matched order from model
        AProduct product = arrived.getThingsList().get(0);
        Order matchedOrder = null;
        for (Order order : orderRepository.findByWhnumAndProductId
                (arrived.getWhnum(), product.getId())) {
            if (order.getQuantity() == product.getCount() && Objects.equals(order.getStatus(), "Purchasing")) {
                matchedOrder = order;
                break;
            }
        }
        if (matchedOrder == null) {
            logger.error("No matched order found for " + product);
            return;
        }

        // add seqnum to ack list
        globalState.addAck(arrived.getSeqnum(), matchedOrder.getId());

        // update the order
        matchedOrder.setStatus("Packing");
        orderRepository.save(matchedOrder);

        // send packing command
        ACommands pack = worldProtoGen.genPackCmd(matchedOrder);
        worldSocketService.sendCommand(pack, pack.getTopack(0).getSeqnum());
    }

    @Async
    public void onReady(APacked ready) {
        logger.info("onReady: " + ready);
        // find matched order from model
        Order matchedOrder = orderRepository.findById(ready.getShipid());
        if (matchedOrder == null) {
            logger.error("No matched order found for " + ready);
            return;
        }
        // add seqnum to ack list
        globalState.addAck(ready.getSeqnum(), matchedOrder.getId());

        // update the order
        matchedOrder.setStatus("Waiting for ups truck");
        orderRepository.save(matchedOrder);
        logger.info("Waiting for truck to arrive...");
        long orderid = matchedOrder.getId();
        // send loading command after truck arrives
        while (orderRepository.findById(orderid).getTruckid() == -1) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread was interrupted", e);
                return;
            }
        }
        matchedOrder = orderRepository.findById(orderid);
        matchedOrder.setStatus("Loading");
        orderRepository.save(matchedOrder);

        logger.info("Truck arrived with truck id: " + matchedOrder.getTruckid());
        ACommands load = worldProtoGen.genLoadCmd(matchedOrder);
        worldSocketService.sendCommand(load, load.getLoad(0).getSeqnum());
    }

    @Async
    public void onLoaded(ALoaded loaded){
        logger.info("onLoaded: " + loaded);

        // find matched order from model
        Order matchedOrder = orderRepository.findById(loaded.getShipid());
        if (matchedOrder == null) {
            logger.error("No matched order found for " + loaded);
            return;
        }

        // add seqnum to ack list
        globalState.addAck(loaded.getSeqnum(), matchedOrder.getId());

        // update the order
        matchedOrder.setStatus("Delivering");
        orderRepository.save(matchedOrder);

        // send delivery command
        AUCommand deliver = upsProtoGen.genToDeliverCmd(matchedOrder);
        try{
            upsSocketService.sendCommand(deliver);
        }catch (IOException e){
            logger.error("Error occur when sending deliver {}", deliver);
        }
    }

    @Async
    public void onError(AErr error) {
        // TODO: handle error
        logger.error("Error received with seqnum: {}", error.getSeqnum());
        logger.error(error.getErr());
    }

}
