package com.example.upsx.service;

import com.example.upsx.protobuf.AmazonUps.*;
import com.example.upsx.model.Order;
import org.springframework.stereotype.Service;

@Service
public class AmazonProtoService {

    public UAConnected genConnedRes(long worldid)  {
        UAConnected uaConnected = UAConnected.newBuilder()
                .setWorldid(worldid)
                .setAck(0)
                .build();
        return uaConnected;
    }

    public UAResponse genTruckArriveRes(Order order) {
        UATruckArrive uaTruckArrive = UATruckArrive.newBuilder()
                .setTruckid(order.getTruckId())
                .setPackageid(order.getPackageId())
                .setWhnum(order.getWhnum())
                .build();

        UAResponse uaResponse = UAResponse.newBuilder()
                .setType(0)
                .setUaTruckArrive(uaTruckArrive)
                .setAck(0)
                .build();

        return uaResponse;
    }

    public UAResponse genDeliveredRes(Order order) {
        UADelivered uaDelivered = UADelivered.newBuilder()
                .setPackageid(order.getPackageId())
                .setDestx(order.getDestX())
                .setDesty(order.getDestY())
                .build();

        UAResponse uaResponse = UAResponse.newBuilder()
                .setType(1)
                .setUaDelivered(uaDelivered)
                .setAck(0)
                .build();

        return uaResponse;
    }
}
