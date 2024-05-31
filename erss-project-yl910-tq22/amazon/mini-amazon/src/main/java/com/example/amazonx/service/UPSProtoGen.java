package com.example.amazonx.service;

import com.example.amazonx.model.WarehouseInfo;
import com.example.amazonx.protobuf.AmazonUps.AUCallTruck;
import com.example.amazonx.protobuf.AmazonUps.AUCommand;
import com.example.amazonx.protobuf.AmazonUps.AUConnect;
import com.example.amazonx.protobuf.AmazonUps.AUOrderDetail;
import com.example.amazonx.protobuf.AmazonUps.AUReadyToDeliver;
import com.example.amazonx.protobuf.AmazonUps.AUWarehouseInfo;
import com.example.amazonx.component.GlobalState;
import com.example.amazonx.model.Order;
import com.example.amazonx.repository.WarehouseInfoRepository;
import org.springframework.stereotype.Service;

@Service
public class UPSProtoGen {

    private final GlobalState globalState;
    private final WarehouseInfoRepository warehouseInfoRepository;

    public UPSProtoGen(GlobalState globalState, WarehouseInfoRepository warehouseInfoRepository) {
        this.globalState = globalState;
        this.warehouseInfoRepository = warehouseInfoRepository;
    }

    public AUConnect genConnCmd(long worldid)  {
        AUConnect auConnect = AUConnect.newBuilder()
                .setWorldid(worldid)
                .setSeqnum(globalState.getUpsSeqNum())
                .build();

        return auConnect;
    }

    public AUCommand genCallTruckCmd(Order order) {

        WarehouseInfo matchWh = warehouseInfoRepository.findById(order.getWhnum());
        AUOrderDetail auOrderDetail = AUOrderDetail.newBuilder()
                .setUpsUserid(order.getUpsUserId())
                .setPdId(order.getProduct().getId())
                .setPdDes(order.getProduct().getDescription())
                .setQuantity(order.getQuantity())
                .build();

        AUWarehouseInfo auWarehouseInfo = AUWarehouseInfo.newBuilder()
                .setWhnum(matchWh.getId())
                .setWhx(matchWh.getX())
                .setWhy(matchWh.getY())
                .build();

        AUCallTruck auCallTruck = AUCallTruck.newBuilder()
                .setPackageid(order.getId())
                .setDestx(order.getDestX())
                .setDesty(order.getDestY())
                .setWh(auWarehouseInfo)
                .setOrder(auOrderDetail)
                .build();

        long seqnum = globalState.getUpsSeqNum();
        globalState.addUPSSeq(seqnum, order.getId());
        AUCommand auCommand = AUCommand.newBuilder()
                .setType(0)
                .setAuCallTruck(auCallTruck)
                .setSeqnum(seqnum)
                .build();

        return auCommand;
    }

    public AUCommand genToDeliverCmd(Order order) {
        AUReadyToDeliver auReadyToDeliver = AUReadyToDeliver.newBuilder()
                .setTruckid(order.getTruckid())
                .setPackageid(order.getId())
                .setDestx(order.getDestX())
                .setDesty(order.getDestY())
                .setWhnum(order.getWhnum())
                .build();

        long seqnum = globalState.getUpsSeqNum();
        globalState.addUPSSeq(seqnum, order.getId());
        AUCommand auCommand = AUCommand.newBuilder()
                .setType(1)
                .setAuReadyDeliver(auReadyToDeliver)
                .setSeqnum(seqnum)
                .build();

        return auCommand;
    }
}
