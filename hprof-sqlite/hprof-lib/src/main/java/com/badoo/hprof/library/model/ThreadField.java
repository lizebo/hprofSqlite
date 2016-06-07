package com.badoo.hprof.library.model;

/**
 * Created by hc on 2016/5/23.
 */
public class ThreadField extends Record {
    private int serialNumber;
    private int id;
    private int stackTraceSerialNumber;
    private int nameStringId;
    private int groupNameId;
    private int parentGroupNameId;

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStackTraceSerialNumber() {
        return stackTraceSerialNumber;
    }

    public void setStackTraceSerialNumber(int stackTraceSerialNumber) {
        this.stackTraceSerialNumber = stackTraceSerialNumber;
    }

    public int getNameStringId() {
        return nameStringId;
    }

    public void setNameStringId(int nameStringId) {
        this.nameStringId = nameStringId;
    }

    public int getGroupNameId() {
        return groupNameId;
    }

    public void setGroupNameId(int groupNameId) {
        this.groupNameId = groupNameId;
    }

    public int getParentGroupNameId() {
        return parentGroupNameId;
    }

    public void setParentGroupNameId(int parentGroupNameId) {
        this.parentGroupNameId = parentGroupNameId;
    }
}
