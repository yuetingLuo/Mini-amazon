package com.example.amazonx.component;

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
    private final AtomicInteger upsSeqNum = new AtomicInteger(0);
    private final AtomicInteger worldSeqNum = new AtomicInteger(0);
    private final ConcurrentHashMap<Long, Long> ToAck = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> ToBeAcked = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> UPSSeq = new ConcurrentHashMap<>();

    public void toggleConnected() {
        this.connected = !this.connected;
    }

    public int getUpsSeqNum() {
        return upsSeqNum.getAndIncrement();
    }

    public int getWorldSeqNum() {
        return worldSeqNum.getAndIncrement();
    }

    public void addAck(Long id, Long package_id) {
        ToAck.put(id, package_id);
    }

    public void addBeAcked(Long id, Long package_id) {
        ToBeAcked.put(id, package_id);
    }

    public void addUPSSeq(Long seq, Long package_id) {
        UPSSeq.put(seq, package_id);
    }

    public long getPackageId(Long seq) {
        return UPSSeq.get(seq);
    }

    public void removeUPSSeq(Long seq) {
        UPSSeq.remove(seq);
    }

    public boolean beAckedContains(Long id) {
        return ToBeAcked.containsKey(id);
    }

    public List<Long> getAllAck() {
        List<Long> ret = new ArrayList<>(ToAck.keySet());
        ToAck.clear();
        return ret;
    }

    public void removeBeAcked(Long id) {
        ToBeAcked.remove(id);
    }
}
