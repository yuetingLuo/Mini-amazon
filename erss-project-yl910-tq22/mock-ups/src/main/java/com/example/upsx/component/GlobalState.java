package com.example.upsx.component;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "global")
public class GlobalState {
    @Getter
    private boolean connected;

    private final AtomicInteger worldSeqNum = new AtomicInteger(0);

    public void toggleConnected() {
        this.connected = !this.connected;
    }

    public int getWorldSeqNum() {
        return worldSeqNum.getAndIncrement();
    }
}
