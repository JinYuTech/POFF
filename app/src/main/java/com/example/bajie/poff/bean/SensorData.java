package com.example.bajie.poff.bean;

/**
 * Created by bajie on 2017/6/21.
 */

public class SensorData {
    private int deviceId;
    private String data;
    private String collectTime;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(String collectTime) {
        this.collectTime = collectTime;
    }
}
