package com.badoo.hprof.library.model;

public class ThreadBlock extends Record{
	int id;
	int serialNum;
	public int getId() {
		return id;
	}
	public ThreadBlock(int id, int serialNum) {
		super();
		this.id = id;
		this.serialNum = serialNum;
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
