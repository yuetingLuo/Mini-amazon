//package com.example.amazonx.service;
//
//
//import com.example.amazonx.component.GlobalState;
//import com.example.amazonx.protobuf.WorldUps.*;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//
//@Service
//public class Mockups {
//    private static final String SERVER_HOST = "67.159.94.27";
//    private static final int SERVER_PORT = 12345;
//    private final GlobalState globalState;
//
//    public Mockups(GlobalState globalState) {
//        this.globalState = globalState;
//    }
//
//    public void sendConnectRequest(long worldid) throws IOException {
//        UConnect uConnect = connect2world(worldid);
//        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
//
//            OutputStream outputStream = socket.getOutputStream();
//            uConnect.writeDelimitedTo(outputStream);
//            outputStream.flush();
//
//            InputStream inputStream = socket.getInputStream();
//            UConnected uConnected = UConnected.parseDelimitedFrom(inputStream);
//            System.out.println("ups world id: "+ uConnected.getWorldid());
//            System.out.println("ups: "+ uConnected.getResult());
//            globalState.toggleConnected();
//        }
//    }
//
//    public UConnect connect2world(long worlidid) {
//        UInitTruck uInitTruck = UInitTruck.newBuilder()
//                .setId(1)
//                .setX(5)
//                .setY(7)
//                .build();
//
//        UConnect uConnect = UConnect.newBuilder()
//                .setIsAmazon(false)
//                .setWorldid(worlidid)
//                .addTrucks(uInitTruck)
//                .build();
//
//        return uConnect;
//    }
//}
