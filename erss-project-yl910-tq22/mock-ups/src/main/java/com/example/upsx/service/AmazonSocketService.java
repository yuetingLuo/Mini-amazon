package com.example.upsx.service;

import com.example.upsx.component.GlobalState;
import com.example.upsx.protobuf.AmazonUps.*;
import com.example.upsx.protobuf.WorldUps.UCommands;
import com.example.upsx.protobuf.WorldUps.UConnected;
import com.example.upsx.protobuf.WorldUps.UConnect;
import com.example.upsx.model.Order;
import com.example.upsx.repository.OrderRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Service
public class AmazonSocketService {

    private static final int PORT = 9000;
    private ServerSocket serverSocket;
    @Autowired
    private GlobalState globalState;
    @Autowired
    private WorldProtoService worldProtoService;
    @Autowired
    private WorldSocketService worldSocketService;
    @Autowired
    private AmazonProtoService amazonProtoService;
    @Autowired
    private OrderRepository orderRepository;
    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;

    @Autowired
    private TaskExecutor taskExecutor;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        taskExecutor.execute(this::startServer);
    }

    public void startServer() {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("Server started on port: " + PORT);
                this.socket = serverSocket.accept();
                this.in = socket.getInputStream();
                this.out = socket.getOutputStream();

                if (!globalState.isConnected()) {
                    AUConnect auConnect = AUConnect.parseDelimitedFrom(this.in);
                    handleConnect(auConnect);
                    taskExecutor.execute(this::listenToAmazon);
                }else{
                    System.out.println("Error in function");
                }
            } catch (IOException e) {
                System.out.println("Error starting server on port " + PORT);
            }
    }

    @Async
    public void listenToAmazon(){
        try {
            while (true) {
                AUCommand auCommand = AUCommand.parseDelimitedFrom(this.in);
                if (auCommand != null) {
                    handleCommand(auCommand);
                }
            }
        } catch (IOException e) {
            System.out.println("error");
        } catch (Exception e) {
//            logger.error("An unexpected error occurred when listen to ups: " + e.getMessage());
        }
    }


    @Async
    protected void handleConnect(AUConnect auConnect) throws IOException {
        UConnect uConnect = worldProtoService.genConnCmd(auConnect.getWorldid());
        System.out.println("Connecting to world...");
        UConnected uConnected = worldSocketService.sendConnectRequest(uConnect);
        if (uConnected.getResult().equals("connected!")) {
            System.out.println("Connected to world!");
            taskExecutor.execute(worldSocketService::listenToServer);
            globalState.toggleConnected();
            UAConnected uaConnected = amazonProtoService.genConnedRes(uConnected.getWorldid());
            uaConnected.writeDelimitedTo(this.out);
        }
    }

    @Async
    protected void handleCommand(AUCommand auCommand) throws IOException {
            if (auCommand.getType() == 0) {
                AUCallTruck auCallTruck = auCommand.getAuCallTruck();
                Order order = new Order(1, auCallTruck.getPackageid(), auCallTruck.getWh().getWhnum(), auCallTruck.getDestx(), auCallTruck.getDesty());
                order.setStatus("Picking");
                orderRepository.save(order);
                System.out.println("Truck is on the way!");
                UCommands goPickup = worldProtoService.genPickupCmd(1, auCallTruck.getWh().getWhnum());
                worldSocketService.sendCommand(goPickup);   // send command to world
                while (orderRepository.findById(order.getId()).getStatus() != "Arrived") {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Truck arrived!");
                UAResponse response = amazonProtoService.genTruckArriveRes(order);
                response.writeDelimitedTo(this.out);
            } else if (auCommand.getType() == 1) {
                AUReadyToDeliver auReadyToDeliver = auCommand.getAuReadyDeliver();
                System.out.println(auReadyToDeliver);
                Order order = orderRepository.findByPackageId(auReadyToDeliver.getPackageid()).get(0);
                order.setStatus("Delivering");
                orderRepository.save(order);
                UCommands goDeliver = worldProtoService.genDeliverCmd(order);
                worldSocketService.sendCommand(goDeliver);   // send command to world
                while (orderRepository.findById(order.getId()).getStatus() != "Delivered") {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                UAResponse response = amazonProtoService.genDeliveredRes(order);
                response.writeDelimitedTo(this.out);
            }

    }

}
