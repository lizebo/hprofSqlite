package com.badoo.hprof.library.model;

/**
 * Created by hc on 2016/5/23.
 */
public class StackFrame extends Record {
    private int id;
    private int methodNameId;
    private int methodSignatureID;
    private int sourceNameID;
    private int classSerialNumber;
    private int state;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMethodNameId() {
        return methodNameId;
    }

    public void setMethodNameId(int methodNameId) {
        this.methodNameId = methodNameId;
    }

    public int getMethodSignatureID() {
        return methodSignatureID;
    }

    public void setMethodSignatureID(int methodSignatureID) {
        this.methodSignatureID = methodSignatureID;
    }

    public int getSourceNameID() {
        return sourceNameID;
    }

    public void setSourceNameID(int sourceNameID) {
        this.sourceNameID = sourceNameID;
    }

    public int getClassSerialNumber() {
        return classSerialNumber;
    }

    public void setClassSerialNumber(int classSerialNumber) {
        this.classSerialNumber = classSerialNumber;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
