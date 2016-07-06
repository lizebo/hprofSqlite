package com.whaley.hprof.sqlitemanager.model;

/**
 * Created by hc on 2016/5/30.
 */
public class TraceItem {
    private String name;
    private int value;

    public TraceItem(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
