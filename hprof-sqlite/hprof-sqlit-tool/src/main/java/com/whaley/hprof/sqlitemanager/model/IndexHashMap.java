package com.whaley.hprof.sqlitemanager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class IndexHashMap extends ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,String>> {
	public void put(int key,int value,String name){
		ConcurrentHashMap<Integer,String> temp;
		if (!containsKey(key)) {
			temp = new ConcurrentHashMap();
		}else {
			temp = get(key);
		}
		temp.put(value,name);
		put(key, temp);
	}
}
