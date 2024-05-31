package com.example.upsx.service;

import com.example.upsx.component.GlobalState;
import com.example.upsx.protobuf.WorldUps.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


@Service
public class WorldSocketService {
    private static final String SERVER_HOST = "67.159.94.27";
    private static final int SERVER_PORT = 12345;
    private static final int TIMEOUT = 60;

    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;
    private WorldHandlerService worldHandlerService;
    private GlobalState globalState;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    public WorldSocketService(WorldHandlerService worldHandlerService
            , GlobalState globalState
    ) {
        this.worldHandlerService = worldHandlerService;
        this.globalState = globalState;
    }

    @PostConstruct
    public void startListening() throws IOException {
        initSocket();
    }

//    @EventListener
//    public void onApplicationEvent(ContextRefreshedEvent event) {
//        taskExecutor.execute(this::listenToServer);
//    }

    @Async
    public void listenToServer() {
        try {
            while (true) {
//                System.out.println("Listening to server");
                UResponses response = UResponses.parseDelimitedFrom(this.in);
                System.out.println(response);
                if (response != null) {
                    worldHandlerService.handleResponse(response);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost");
        }
    }

    public void initSocket() throws IOException {
        this.socket = new Socket(SERVER_HOST, SERVER_PORT);
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    public UConnected sendConnectRequest(UConnect uConnect) throws IOException {
        uConnect.writeDelimitedTo(this.out);
        this.out.flush();
        return UConnected.parseDelimitedFrom(this.in);
    }

    @Async
    public void sendCommand(UCommands aCommand) {
        try {
            aCommand.writeDelimitedTo(this.out);
            this.out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
