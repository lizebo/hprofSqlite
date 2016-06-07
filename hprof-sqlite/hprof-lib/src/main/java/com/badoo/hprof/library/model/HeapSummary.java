package com.badoo.hprof.library.model;

/**
 * Created by hc on 2016/5/23.
 */
public class HeapSummary extends Record {
    private int liveByteNum;
    private int liveInstanceNum;
    private int allocByteNum;
    private int allocInstanceNum;

    public int getLiveByteNum() {
        return liveByteNum;
    }

    public void setLiveByteNum(int liveByteNum) {
        this.liveByteNum = liveByteNum;
    }

    public int getLiveInstanceNum() {
        return liveInstanceNum;
    }

    public void setLiveInstanceNum(int liveInstanceNum) {
        this.liveInstanceNum = liveInstanceNum;
    }

    public int getAllocByteNum() {
        return allocByteNum;
    }

    public void setAllocByteNum(int allocByteNum) {
        this.allocByteNum = allocByteNum;
    }

    public int getAllocInstanceNum() {
        return allocInstanceNum;
    }

    public void setAllocInstanceNum(int allocInstanceNum) {
        this.allocInstanceNum = allocInstanceNum;
    }
}

