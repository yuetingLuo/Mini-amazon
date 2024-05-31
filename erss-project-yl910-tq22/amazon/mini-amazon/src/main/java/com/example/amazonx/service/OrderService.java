package com.example.amazonx.service;

import com.example.amazonx.model.Order;
import com.example.amazonx.model.Product;
import com.example.amazonx.model.User;
import com.example.amazonx.model.WarehouseInfo;
import com.example.amazonx.protobuf.WorldAmazon.ACommands;
import com.example.amazonx.protobuf.AmazonUps.AUCommand;
import com.example.amazonx.protobuf.AmazonUps.UAResponse;
import com.example.amazonx.repository.OrderRepository;
import com.example.amazonx.repository.ProductRepository;
import com.example.amazonx.repository.UserRepository;
import com.example.amazonx.repository.WarehouseInfoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final WarehouseInfoRepository warehouseInfoRepository;
    private final UPSProtoGen upsProtoGen;
    private final WorldProtoGen worldProtoGen;
    private final UPSSocketService upsSocketService;
    private final WorldSocketService worldSocketService;

    @Autowired
    public OrderService(OrderRepository orderRepository
            , ProductRepository productRepository
            , UserRepository userRepository
            , WarehouseInfoRepository warehouseInfoRepository
            , UPSProtoGen upsProtoGen
            , UPSSocketService upsSocketService
            , WorldProtoGen worldProtoGen
            , WorldSocketService worldSocketService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.warehouseInfoRepository = warehouseInfoRepository;
        this.upsProtoGen = upsProtoGen;
        this.upsSocketService = upsSocketService;
        this.worldProtoGen = worldProtoGen;
        this.worldSocketService = worldSocketService;
    }

    private int getShortestWhnum(int x, int y){
        List<WarehouseInfo> whlist = warehouseInfoRepository.findAll();
        int minidistance = Integer.MAX_VALUE;
        int whnum = -1;
        for(int i = 0; i < whlist.size(); i++){
            int dis = Math.abs(x-whlist.get(i).getX())+Math.abs(y-whlist.get(i).getY());
            if(dis < minidistance){
                whnum = whlist.get(i).getId();
                minidistance = dis;
            }
        }
        return whnum;
    }
    @Async
    public void processOrder(String upsUserid, long productid, int quantity,
                             int destX, int destY
                            , String username
    ) throws IOException {
        // create order & save to db
        Product product = productRepository.findById(productid).get(0);
        int whnum = getShortestWhnum(destX, destY);
        User user = userRepository.findByUsername(username);
        Order order = new Order(upsUserid, product, quantity, whnum, destX, destY
                , user
        );
        orderRepository.save(order);
//        user.addOrder(order);
//        userRepository.save(user);
        // purchase command
        ACommands purchase = worldProtoGen.genPurchaseCmd(order);

        worldSocketService.sendCommand(purchase, purchase.getBuy(0).getSeqnum());

        // call truck
        orderRepository.save(order);
        AUCommand callTruck = upsProtoGen.genCallTruckCmd(order);
        upsSocketService.sendCommand(callTruck);

    }

    public List<Order> listOrders(String username) {
        List<Order> orders = orderRepository.findAllByUserUsername(username);
//        System.out.println(orders);
        return orders;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

}
