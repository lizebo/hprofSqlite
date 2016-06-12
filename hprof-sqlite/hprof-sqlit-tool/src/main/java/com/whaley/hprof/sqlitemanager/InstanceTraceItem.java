package com.whaley.hprof.sqlitemanager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.tree.TreeNode;

/**
 * Created by hc on 2016/6/1.
 */
public class InstanceTraceItem implements TreeNode{
    private int id;
    private String name;
    private int length;
    private ArrayList<InstanceTraceItem> traceItems;

    public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public InstanceTraceItem() {
        traceItems = new ArrayList<InstanceTraceItem>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<InstanceTraceItem> getTraceItems() {
        return traceItems;
    }

    public void setTraceItems(ArrayList<InstanceTraceItem> traceItems) {
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
		return traceItems.get(childIndex);
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return traceItems.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		// TODO Auto-generated method stub
		return traceItems.indexOf(node);
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
		return name;
	}
}
