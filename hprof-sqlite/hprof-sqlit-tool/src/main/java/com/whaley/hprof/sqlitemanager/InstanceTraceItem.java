package com.whaley.hprof.sqlitemanager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.tree.TreeNode;

/**
 * Created by hc on 2016/6/1.
 */
public class InstanceTraceItem implements TreeNode{
    private int id;
    private String name;
    private int length;
    private String fieldName;
    private HashSet<InstanceTraceItem> traceItems;

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

	public InstanceTraceItem() {
        traceItems = new HashSet();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashSet<InstanceTraceItem> getTraceItems() {
        return traceItems;
    }

    public void setTraceItems(HashSet<InstanceTraceItem> traceItems) {
        this.traceItems = traceItems;
    }
    public void addTrace(InstanceTraceItem item){
        traceItems.add(item);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void print(){
        System.out.println(name+"\n");
        Iterator<InstanceTraceItem> iterator = traceItems.iterator();
        while (iterator.hasNext()){
            InstanceTraceItem traceItem = iterator.next();
            traceItem.print();
        }
    }

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
		return (InstanceTraceItem)items.get(childIndex);
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
		items.addAll(traceItems);
		return items.indexOf(node);
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
		if (length>0) {
			if (fieldName!=null) {
				return name+"["+fieldName+"]"+" size:"+length+"byte"+" id:"+id;
			}
			return name+" size:"+length+"byte";
		}

		return name;
	}
	
	public boolean contains(int id){
		Iterator<InstanceTraceItem> iterator = traceItems.iterator();
		while (iterator.hasNext()) {
			InstanceTraceItem item = iterator.next();
			if (item.getId()==id) {
				return true;
			}
		}
		return false;
	}
}
