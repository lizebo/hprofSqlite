package com.badoo.hprof.library.model;

public class NativeStackModel extends Record{
	
	public NativeStackModel(int id, int serialNum) {
		super();
		this.id = id;
		this.serialNum = serialNum;
	}
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
}
