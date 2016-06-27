package com.badoo.hprof.library.model;

public class LocalModel extends Record{
	int id;
	int serialNum;
	public LocalModel(int id, int serialNum, int tracenum) {
		super();
		this.id = id;
		this.serialNum = serialNum;
		this.tracenum = tracenum;
	}
	int tracenum;
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
	public int getTracenum() {
		return tracenum;
	}
	public void setTracenum(int tracenum) {
		this.tracenum = tracenum;
	}
}
