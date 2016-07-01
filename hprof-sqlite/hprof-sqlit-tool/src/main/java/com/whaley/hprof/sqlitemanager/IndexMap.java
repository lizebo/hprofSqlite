package com.whaley.hprof.sqlitemanager;

import jdk.internal.dynalink.beans.StaticClass;

public class IndexMap{
	public final static int TYPE_INSTANCE = 1;
	public final static int TYPE_CLASS = 2;
	public final static int TYPE_ARR = 3;
	public Integer key;
	public Integer value;
	public String fieldname;
	public int type;

	public IndexMap() {
		// TODO Auto-generated constructor stub
	}

	public IndexMap(int key, int value, String fieldname, int type) {
		super();
		this.key = key;
		this.value = value;
		this.fieldname = fieldname;
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof IndexMap) {
			IndexMap temp = (IndexMap) obj;
			return this.key==temp.key&&this.value==temp.value;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return key.hashCode()+value.hashCode();
	}

}
