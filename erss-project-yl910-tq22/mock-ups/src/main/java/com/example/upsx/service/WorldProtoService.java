package com.example.upsx.service;

import com.example.upsx.component.GlobalState;
import com.example.upsx.model.TruckInfo;
import com.example.upsx.protobuf.WorldUps.UCommands;
import com.example.upsx.protobuf.WorldUps.UConnect;
import com.example.upsx.protobuf.WorldUps.UInitTruck;
import com.example.upsx.protobuf.WorldUps.UGoPickup;
import com.example.upsx.protobuf.WorldUps.UGoDeliver;
import com.example.upsx.protobuf.WorldUps.UDeliveryLocation;
import com.example.upsx.model.Order;
import org.springframework.stereotype.Service;
import com.example.upsx.repository.TruckInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class WorldProtoService {

    @Autowired
    private TruckInfoRepository truckInfoRepository;
    @Autowired
    private GlobalState globalState;

    public UConnect genConnCmd(long worldid) {
        TruckInfo truckInfo = new TruckInfo(1, 1);
        truckInfoRepository.save(truckInfo);
        UInitTruck uInitTruck = UInitTruck.newBuilder()
                .setId(truckInfo.getId())
                .setX(truckInfo.getX())
                .setY(truckInfo.getY())
                .build();

        UConnect uConnect = UConnect.newBuilder()
                .setIsAmazon(false)
                .addTrucks(uInitTruck)
                .setWorldid(worldid)
                .build();

        return uConnect;
    }

    public UCommands genPickupCmd(int truckid, int whid) {

        UGoPickup uGoPickup = UGoPickup.newBuilder()
                .setTruckid(truckid)
                .setWhid(whid)
                .setSeqnum(globalState.getWorldSeqNum())
                .build();

        UCommands uCommands = UCommands.newBuilder()
                .addPickups(uGoPickup)
                .setSimspeed(100)
                .build();

        return uCommands;
    }

    public UCommands genDeliverCmd(Order order) {
        UDeliveryLocation uDeliveryLocation = UDeliveryLocation.newBuilder()
                .setPackageid(order.getPackageId())
                .setX(order.getDestX())
                .setY(order.getDestY())
                .build();

        UGoDeliver uGoDeliver = UGoDeliver.newBuilder()
                .setTruckid(order.getTruckId())
                .addPackages(uDeliveryLocation)
                .setSeqnum(globalState.getWorldSeqNum())
                .build();

        UCommands uCommands = UCommands.newBuilder()
                .addDeliveries(uGoDeliver)
                .setSimspeed(100)
                .build();

        return uCommands;
    }
}
