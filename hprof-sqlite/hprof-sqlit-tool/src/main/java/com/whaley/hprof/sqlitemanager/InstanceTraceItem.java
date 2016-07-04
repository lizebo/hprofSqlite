package com.whaley.hprof.sqlitemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.tree.TreeNode;

/**
 * Created by hc on 2016/6/1.
 */
public class InstanceTraceItem implements TreeNode{
    private int id;
    private String name;
    private int length=0;
    private String fieldName;
    private Set<InstanceTraceItem> traceItems;
    private Hashtable<Integer, GCRootPath> rootPath;

    public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@SuppressWarnings("unchecked")
	public InstanceTraceItem() {
//        traceItems = new HashSet<InstanceTraceItem>();
        traceItems = Collections.synchronizedSet(new HashSet());
        rootPath = new Hashtable<Integer, GCRootPath>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<InstanceTraceItem> getTraceItems() {
        return traceItems;
    }

    public void setTraceItems(HashSet traceItems) {
        this.traceItems = traceItems;
    }
    public void addTrace(InstanceTraceItem item,int rootId,int path){
		if (id==item.getId()) {
			return;
		}
    	GCRootPath temp = rootPath.get(rootId);   	
    	if (temp!=null&&temp.path>path) {
//			traceItems.remove(temp.item);
	        traceItems.add(item);
	        rootPath.replace(rootId,new GCRootPath(path,item));
		}else if (temp==null) {
			traceItems.add(item);
			rootPath.put(rootId,new GCRootPath(path,item));
		}
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public void print(){
//        System.out.println(name+"\n");
//        Iterator iterator = traceItems.iterator();
//        while (iterator.hasNext()){
//            InstanceTraceItem traceItem = iterator.next();
//            traceItem.print();
//        }
//    }

	@Override
	public Enumeration children() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		// TODO Auto-generated method stub
		ArrayList items = new ArrayList();
		items.addAll(traceItems);
		return (TreeNode) items.get(childIndex);
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return traceItems.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		// TODO Auto-generated method stub
		ArrayList items = new ArrayList();
//		items.addAll(traceItems);
		return 0;
	}

	@Override
	public TreeNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return traceItems.isEmpty();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
//		if (length>0) {
			if (fieldName!=null) {
				return name+"["+fieldName+"]"+" size:"+length+"byte"+" id:"+id;
			}
			return name+" size:"+length+"byte"+" id:"+id;
//		}

//		return name;
	}
	
	
	class GCRootPath{
		int path;
		InstanceTraceItem item;
		public GCRootPath(int path, InstanceTraceItem item) {
			super();
			this.path = path;
			this.item = item;
		}
		
	}

	public void addTrace(InstanceTraceItem item) {
		// TODO Auto-generated method stub
		traceItems.add(item);
	}
	public void addTrace(InstanceTraceItem item,int rootId) {
		// TODO Auto-generated method stub
		if (id==item.getId()) {
			return;
		}
		int path = item.getPathLen(rootId)+1; 
		GCRootPath temp = rootPath.get(rootId);
//		traceItems.remove(temp.item);
		rootPath.replace(rootId, new GCRootPath(path, item));
		traceItems.add(item);
		
	}
	
	public Hashtable<Integer, GCRootPath> getRootPath(){
		return rootPath;
	}
	
	public int getPathLen(int rootId){
		GCRootPath temp = rootPath.get(rootId);
		if (temp!=null) {
			return temp.path;
		}
		return -1;
	}

	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof InstanceTraceItem) {
			return id==((InstanceTraceItem)arg0).getId();
		}
		return super.equals(arg0);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return id;
	}
	
	
}
