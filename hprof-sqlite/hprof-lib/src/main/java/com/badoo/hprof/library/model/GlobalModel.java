package com.badoo.hprof.library.model;

public class GlobalModel extends Record{
	int id;
	int refId;
	public GlobalModel(int id, int refId) {
		super();
		this.id = id;
		this.refId = refId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRefId() {
		return refId;
	}
	public void setRefId(int refId) {
		this.refId = refId;
	}
}
