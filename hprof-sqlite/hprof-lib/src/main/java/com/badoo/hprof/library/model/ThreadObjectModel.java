package com.badoo.hprof.library.model;

public class ThreadObjectModel extends Record{
	int id;
	int serialNum;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSerialNum() {
		return serialNum;
	}
	public void setSerialNum(int serialNum) {
		this.serialNum = serialNum;
	}
	public int getTraceNum() {
		return traceNum;
	}
	public void setTraceNum(int traceNum) {
		this.traceNum = traceNum;
	}
	public ThreadObjectModel(int id, int serialNum, int traceNum) {
		super();
		this.id = id;
		this.serialNum = serialNum;
		this.traceNum = traceNum;
	}
	int traceNum;
}
