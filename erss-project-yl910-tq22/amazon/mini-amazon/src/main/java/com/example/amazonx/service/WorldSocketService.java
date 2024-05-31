package com.example.amazonx.service;

import com.example.amazonx.component.GlobalState;
import com.example.amazonx.model.WarehouseInfo;
import com.example.amazonx.protobuf.WorldAmazon;
import com.example.amazonx.protobuf.WorldAmazon.*;
import com.example.amazonx.repository.WarehouseInfoRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class WorldSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WorldSocketService.class);
    private static final String SERVER_HOST = "vcm-39015.vm.duke.edu";
    private static final int SERVER_PORT = 23456;
    private static final int TIMEOUT = 60;
    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;
    private final WarehouseInfoRepository warehouseInfoRepository;
    private final WorldProtoGen worldProtoGen;
    private WorldHandler worldHandler;
    private GlobalState globalState;
    //    private final Mockups mockups;
    private final UPSSocketService upsSocketService;
    private final TaskExecutor taskExecutor;

    @Autowired
    public WorldSocketService(WarehouseInfoRepository warehouseInfoRepository, WorldProtoGen worldProtoGen, WorldHandler worldHandler
            , GlobalState globalState
//            , Mockups mockups,
            , UPSSocketService upsSocketService
            , TaskExecutor taskExecutor
    ) {
        this.warehouseInfoRepository = warehouseInfoRepository;
        this.worldProtoGen = worldProtoGen;
        this.worldHandler = worldHandler;
        this.globalState = globalState;
//        this.mockups = mockups;
        this.upsSocketService = upsSocketService;
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    public void startListening() throws IOException {
        initSocket();
        taskExecutor.execute(this::connectAndListen);
    }

    @Async
    void connectAndListen() {
        try {
            logger.info("Attempting to connect to the server...");
            long worldid = connect2world();
            //globalState.toggleConnected();
            taskExecutor.execute(() -> asyncConnectUPS(worldid));
            //mockups.sendConnectRequest(worldid);
            taskExecutor.execute(this::listenToServer);
        } catch (IOException e) {
            logger.error("Failed to connect or listen to the server", e);
        }
    }

    private void asyncConnectUPS(long worldid){
        upsSocketService.sendConnectRequest(worldid);
    }

    private ArrayList<AInitWarehouse> initwh() {

        List<WarehouseInfo> warehouseInfos = Arrays.asList(
                new WarehouseInfo(-100, 0),
                new WarehouseInfo(0, -100),
                new WarehouseInfo(100, 0),
                new WarehouseInfo(0, 100)
        );

        ArrayList<AInitWarehouse> aInitWarehouses = new ArrayList<>();
        for (WarehouseInfo warehouseInfo : warehouseInfos) {
            warehouseInfoRepository.save(warehouseInfo);
            AInitWarehouse aInitWarehouse = AInitWarehouse.newBuilder()
                    .setId(warehouseInfo.getId())
                    .setX(warehouseInfo.getX())
                    .setY(warehouseInfo.getY())
                    .build();

            aInitWarehouses.add(aInitWarehouse);
        }
        return aInitWarehouses;
    }

    private long connect2world() throws IOException {
        AConnect aConnect = worldProtoGen.genConnCmd(initwh());
        AConnected aConnected = sendConnectRequest(aConnect);
        if (aConnected != null && "connected!".equals(aConnected.getResult())) {
            long worldid = aConnected.getWorldid();
            logger.info("amazon: " + worldid + " " + aConnected.getResult());
            return worldid;
        } else {
            throw new IOException("Unable to connect to world server.");
        }
    }

    public void initSocket() throws IOException {
        this.socket = new Socket(SERVER_HOST, SERVER_PORT);
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    @Async
    public void listenToServer() {
        try {
            while (true) {
                logger.info("Listening to server");
                AResponses response = AResponses.parseDelimitedFrom(this.in);
                if (response != null) {
                    worldHandler.handleResponse(response);
                }
            }
        } catch (IOException e) {
            logger.error("Connection lost");
        } catch (Exception e) {
            logger.error("An unexpected error occurred: " + e.getMessage());
        }
    }

    public AConnected sendConnectRequest(AConnect aConnect) throws IOException {
        aConnect.writeDelimitedTo(this.out);
        return AConnected.parseDelimitedFrom(this.in);
    }

    @Async
    public void sendCommand(ACommands aCommand, long ackNum) {
        try {
            aCommand.writeDelimitedTo(this.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable commandTask = () -> {
            if (globalState.beAckedContains(ackNum)) {
                try {
                    aCommand.writeDelimitedTo(this.out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                scheduler.shutdown();
            }
        };

        scheduler.scheduleWithFixedDelay(commandTask, 0, 10, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
        }, TIMEOUT, TimeUnit.SECONDS);
    }

}
