package com.example.amazonx.service;

import com.example.amazonx.model.WarehouseInfo;
import com.example.amazonx.protobuf.WorldAmazon.*;
import com.example.amazonx.model.Order;
import com.example.amazonx.component.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorldProtoGen {
    private static final Logger logger = LoggerFactory.getLogger(WorldProtoGen.class);

    private final GlobalState globalState;

    @Autowired
    public WorldProtoGen(GlobalState globalState) {
        this.globalState = globalState;
    }

    public AConnect genConnCmd(ArrayList<AInitWarehouse> whlist) {

        AConnect aConnect = AConnect.newBuilder()
                .setIsAmazon(true)
                .addAllInitwh(whlist)
                .build();

        return aConnect;
    }

    public ACommands genPurchaseCmd(Order order) {
        AProduct aProduct = AProduct.newBuilder()
                .setId(order.getProduct().getId())
                .setDescription(order.getProduct().getDescription())
                .setCount(order.getQuantity())
                .build();

        long seqNum = globalState.getWorldSeqNum();
        globalState.addBeAcked(seqNum, order.getId());
        APurchaseMore aPurchaseMore = APurchaseMore.newBuilder()
                .setWhnum(order.getWhnum())
                .addThings(aProduct)
                .setSeqnum(seqNum)
                .build();
        logger.info("purchase more seq{}",seqNum);
        List<Long> acks = globalState.getAllAck();
        ACommands.Builder aCommandBuilder = ACommands.newBuilder()
                .addBuy(aPurchaseMore)
                .setSimspeed(300000);
        if (!acks.isEmpty()) {
            aCommandBuilder.addAllAcks(acks);
        }

        return aCommandBuilder.build();
    }

    public ACommands genPackCmd(Order order) {
        AProduct aProduct = AProduct.newBuilder()
                .setId(order.getProduct().getId())
                .setDescription(order.getProduct().getDescription())
                .setCount(order.getQuantity())
                .build();

        long seqNum = globalState.getWorldSeqNum();
        logger.info("pack seq{}",seqNum);
        globalState.addBeAcked(seqNum, order.getId());
        APack aPack = APack.newBuilder()
                .setWhnum(order.getWhnum())
                .addThings(aProduct)
                .setShipid(order.getId())
                .setSeqnum(seqNum)
                .build();

        List<Long> acks = globalState.getAllAck();
        ACommands.Builder aCommandBuilder = ACommands.newBuilder()
                .addTopack(aPack)
                .setSimspeed(300000);

        if (!acks.isEmpty()) {
            aCommandBuilder.addAllAcks(acks);
        }
        return aCommandBuilder.build();
    }

    public ACommands genLoadCmd(Order order) {

        long seqNum = globalState.getWorldSeqNum();
        globalState.addBeAcked(seqNum, order.getId());
        APutOnTruck aPutOnTruck = APutOnTruck.newBuilder()
                .setWhnum(order.getWhnum())
                .setTruckid(order.getTruckid())
                .setShipid(order.getId())
                .setSeqnum(seqNum)
                .build();

        logger.info("load seq{}",seqNum);
        List<Long> acks = globalState.getAllAck();
        ACommands.Builder aCommandBuilder = ACommands.newBuilder()
                .addLoad(aPutOnTruck)
                .setSimspeed(300000);

        if (!acks.isEmpty()) {
            aCommandBuilder.addAllAcks(acks);
        }
        return aCommandBuilder.build();
    }
}
