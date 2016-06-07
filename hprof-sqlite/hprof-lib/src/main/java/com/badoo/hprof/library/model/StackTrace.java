package com.badoo.hprof.library.model;

import java.util.ArrayList;

/**
 * Created by hc on 2016/5/23.
 */
public class StackTrace extends Record {
    private int stackTraceSerialNumber;
    private int threadSerialNumber;
    private int frameNum;
    private ArrayList<Integer> frameIds;

    public int getThreadSerialNumber() {
        return threadSerialNumber;
    }

    public void setThreadSerialNumber(int threadSerialNumber) {
        this.threadSerialNumber = threadSerialNumber;
    }

    public int getFrameNum() {
        return frameNum;
    }

    public void setFrameNum(int frameNum) {
        this.frameNum = frameNum;
    }

    public ArrayList<Integer> getFrameIds() {
        return frameIds;
    }

    public void setFrameIds(ArrayList<Integer> frameIds) {
        this.frameIds = frameIds;
    }

    public int getStackTraceSerialNumber() {
        return stackTraceSerialNumber;
    }

    public void setStackTraceSerialNumber(int stackTraceSerialNumber) {
        this.stackTraceSerialNumber = stackTraceSerialNumber;
    }
}
