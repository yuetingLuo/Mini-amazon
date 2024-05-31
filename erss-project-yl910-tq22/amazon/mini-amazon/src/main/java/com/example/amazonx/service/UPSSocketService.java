package com.example.amazonx.service;

import com.example.amazonx.component.GlobalState;
import com.example.amazonx.protobuf.AmazonUps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Service
public class UPSSocketService {
    private static final String SERVER_HOST = "vcm-40053.vm.duke.edu";
    private static final int SERVER_PORT = 9000;
    private final UPSProtoGen upsProtoGen;
    private final GlobalState globalState;
    private final UPSHandler upsHandler;

    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;
    private final TaskExecutor taskExecutor;
    private static final Logger logger = LoggerFactory.getLogger(UPSSocketService.class);

    @Autowired
    public UPSSocketService(UPSProtoGen ups, GlobalState globalState, UPSHandler upsHandler, TaskExecutor taskExecutor){
        this.upsProtoGen =ups;
        this.globalState = globalState;
        this.upsHandler = upsHandler;
        this.taskExecutor = taskExecutor;
    }

    private void socketInit(){
        boolean connect = false;
        while(!connect){
            try{
                this.socket = new Socket(SERVER_HOST, SERVER_PORT);
                this.in = socket.getInputStream();
                this.out = socket.getOutputStream();
                connect = true;
            }catch (IOException e){
                logger.error("UPS connect failed, retry");
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException ie){
                    logger.error("Thread interrupt error");
                }
            }
        }
    }

    public void sendConnectRequest(long worldid) {
        boolean connected = false;
        while (!connected) {
            try {
                socketInit();
                AUConnect auConnect = upsProtoGen.genConnCmd(worldid);
                while (!globalState.isConnected()) {
                    auConnect.writeDelimitedTo(this.out);
                    UAConnected uaConnected = UAConnected.parseDelimitedFrom(this.in);
                    if (uaConnected != null && uaConnected.getWorldid() == worldid) {
                        globalState.toggleConnected();
                        logger.info("Ups also connected to world {}", worldid);
                    } else {
                        logger.error("Connect ups error: Invalid world id or no response");
                    }
                }
                taskExecutor.execute(this::listenToUps);
                connected = true;
            } catch (IOException e) {
                logger.error("An I/O error occurred when connect to ups {}", e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    logger.error("Thread interrupt error");
                }
            }
        }
    }

    @Async
    public void listenToUps(){
        try {
            while (true) {
                logger.info("Listening to ups");
                UAResponse response = UAResponse.parseDelimitedFrom(this.in);
                logger.info("Received response: {}", response);
                Thread.sleep(5000);
                if (response != null) {
                    upsHandler.handleResponse(response);
                }
            }
        } catch (IOException e) {
            logger.error("Connection to ups lost");
        } catch (Exception e) {
            logger.error("An unexpected error occurred when listen to ups: " + e.getMessage());
        }
    }

    public void sendCommand(AUCommand auCommand) throws IOException {
        logger.debug("Sending command to ups {}",auCommand);
        try {
            auCommand.writeDelimitedTo(this.out);
//            this.out.flush();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
