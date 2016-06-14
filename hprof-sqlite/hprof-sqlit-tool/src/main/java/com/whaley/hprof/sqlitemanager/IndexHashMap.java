package com.whaley.hprof.sqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("serial")
public class IndexHashMap extends HashMap<Integer, HashMap<Integer,String>> {
	public void put(int key,int value,String name){
		HashMap<Integer,String> temp;
		if (!containsKey(key)) {
			temp = new HashMap();
		}else {
			temp = get(key);
		}
		temp.put(value,name);
		put(key, temp);
	}
}
