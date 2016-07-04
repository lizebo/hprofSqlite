package com.whaley.hprof.sqlitemanager;

import com.badoo.hprof.library.Tag;
import com.badoo.hprof.library.heap.HeapTag;
import com.badoo.hprof.library.model.*;
import com.google.common.collect.Multiset.Entry;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.xml.internal.stream.Entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jdk.internal.org.objectweb.asm.tree.analysis.Value;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;

/**
 * Created by hc on 2016/5/20.
 */
public class SqliteManager {
	private static SqliteManager instance = null;
	Connection conn = null;
	private int totalSize = 0;
	private IndexHashMap instanceMap; // 实例引用映射表
	private IndexHashMap classMap; // 类引用映射表（static变量-》class）
	private IndexHashMap arrMap; // 数组引用映射表
	private IndexHashMap mapInstance; // 实例引用映射表
	private IndexHashMap mapClass; // 类引用映射表（static变量-》class）
	private IndexHashMap mapArr; // 数组引用映射表
	private HashMap<Integer, String> indexClassName; // classId->classname
	private Hashtable<Integer, ObjectArray> objArr; // objectArr表
	private HashMap<Integer, InstanceTraceItem> initedTraceItems; // 记录已查找过的instance路径

	private HashMap<Integer, List<InstanceField>> classFiled; // class instance
																// fields表
	private HashMap<Integer, Integer> classLength;
	private HashMap<Integer, Integer> instanceToClass; // instance->class表
	private HashSet<Integer> referClasses; // java.lang.ref.*类表
	private HashMap<Integer, Instance> finalizersInstances; // java.lang.ref.*类实例表
	private HashMap<Integer, Instance> instances; // 总实例表
	private Hashtable<Integer, Instance> gcRootInstance; // gc root引用的instance
	private Hashtable<Integer, String> gcRoot; // gc root Id
												// 包括instanceId、classId
	private Hashtable<Integer, ObjectArray> rootObjArr; // root引用的objarr
	private IndexHashMap rootClassMap; // gc root class

	HashSet<IndexMap> instanceRefMap;
	private ArrayList<Integer> systemClass;

	private SqliteManager() {
		classFiled = new HashMap<Integer, List<InstanceField>>();
		classLength = new HashMap<Integer, Integer>();
		instanceMap = new IndexHashMap();
		classMap = new IndexHashMap();
		arrMap = new IndexHashMap();
		mapArr = new IndexHashMap();
		mapClass = new IndexHashMap();
		mapInstance = new IndexHashMap();

		indexClassName = new HashMap<Integer, String>();
		referClasses = new HashSet<Integer>();
		systemClass = new ArrayList<Integer>();
		instanceToClass = new HashMap<Integer, Integer>();
		finalizersInstances = new HashMap<Integer, Instance>();
		// gcRootInstanceOrigin = new HashMap<Integer, Instance>();
		gcRootInstance = new Hashtable();
		// gcRootIds = new HashSet<Integer>();
		instances = new HashMap<Integer, Instance>();
		// classToClassLoader = new HashMap<Integer, Integer>();
		gcRoot = new Hashtable<Integer, String>();

		// objArrIndex = new IndexHashMap();
		instanceRefMap = new HashSet<IndexMap>();
		objArr = new Hashtable<Integer, ObjectArray>();
		rootObjArr = new Hashtable<Integer, ObjectArray>();
		// instanceIndexMap = new Hashtable<Integer, InstanceTraceItem>();
		initedTraceItems = new HashMap<Integer, InstanceTraceItem>();
		rootClassMap = new IndexHashMap();
	}

	private static synchronized void syncInit() {
		if (instance == null) {
			instance = new SqliteManager();
		}
	}

	public static SqliteManager getInstance() {
		if (instance == null) {
			syncInit();
		}
		return instance;
	}

	/**
	 * @param tag
	 * @param obj
	 */
	public void insertData(int tag, Record obj) {
		StringBuilder buffer = new StringBuilder();
		PreparedStatement insertState = null;
		int i;
		try {
			conn.setAutoCommit(false);
			switch (tag) {

			case Tag.STRING:
				buffer.append("insert into ").append(SQLDOMAIN.TABLE_STRING)
						.append(" values (")
						.append(((HprofString) obj).getId()).append(",'")
						.append(((HprofString) obj).getValue()).append("');");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				if (insertState != null) {
					insertState.close();
				}
				break;
			case Tag.LOAD_CLASS:
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 链接数据库，并读取各个映射表到内存
	 * 
	 * @param dbName
	 */
	public void connect(String dbName) {
		try {
			Class.forName("org.sqlite.JDBC");
			String conName = "jdbc:sqlite:" + dbName;
			if (conn!=null&&!conn.isClosed()) {
				conn.close();
			}
			conn = DriverManager.getConnection(conName);
			conn.setAutoCommit(false);
			PreparedStatement indexStatement = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD);
			ResultSet set = indexStatement.executeQuery();
			instanceRefMap.clear();
			while (set.next()) {
				int key = set.getInt("instance_id");
				int value = set.getInt("value");
				int type = set.getInt("type");
				String fieldName = set.getString("field_name");
				instanceRefMap.add(new IndexMap(key,value,fieldName,type));
				// indexValueToId.put(key, value, fieldName);
			}
			PreparedStatement classStatement = conn
					.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS);
			ResultSet classResultSet = classStatement.executeQuery();
			indexClassName.clear();
			while (classResultSet.next()) {
				int classId = classResultSet.getInt("id");
				int classLoaderId1 = classResultSet.getInt("class_load_id");
				String name = classResultSet.getString("name");
				indexClassName.put(classId, name);
			}
			indexStatement.close();
			set.close();
			classResultSet.close();
			classStatement.close();
			PreparedStatement instanceStatement = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.TABLE_INSTANCE);
			ResultSet instanceResultSet = instanceStatement.executeQuery();
			instanceToClass.clear();
			instances.clear();
			while (instanceResultSet.next()) {
				int id = instanceResultSet.getInt("id");
				int classId = instanceResultSet.getInt("class_id");
				instanceToClass.put(id, classId);
				Instance temp = new Instance(id, 0, classId, 0, null);
				instances.put(id, temp);
			}
			instanceResultSet.close();
			instanceStatement.close();
			PreparedStatement gcRootStatement = conn
					.prepareStatement("select * from " + SQLDOMAIN.ROOT_IDS);
			ResultSet gcSet = gcRootStatement.executeQuery();
			gcRoot.clear();
			while (gcSet.next()) {
				gcRoot.put(gcSet.getInt("id"), gcSet.getString("type"));

			}
			gcRootStatement.close();
			gcSet.close();
			PreparedStatement objarStatement = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.TABLE_OBJARRAY);
			ResultSet objArrSet = objarStatement.executeQuery();
			while (objArrSet.next()) {
				objArr.put(
						objArrSet.getInt("id"),
						new ObjectArray(objArrSet.getInt("id"), objArrSet
								.getInt("element_class_id"), objArrSet
								.getInt("count")));

			}
			objArrSet.close();
			objarStatement.close();
			initMaps(instanceRefMap);
			initGCMap();
//			PreparedStatement indexInstance
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 建立一个数据库名zieckey.db的连接，如果不存在就在当前目录下创建之

	}

	/**
	 * 建表
	 * 
	 * @param dbName
	 */
	public void createTables(String dbName) {
		try {
			Class.forName("org.sqlite.JDBC");
			// 建立一个数据库名zieckey.db的连接，如果不存在就在当前目录下创建之
			String conName = "jdbc:sqlite:" + dbName + ".db";
			if (conn!=null&&!conn.isClosed()) {
				conn.close();
			}
			conn = DriverManager.getConnection(conName);
			conn.setAutoCommit(false);
			PreparedStatement delStringTable = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_STRING + ";");
			delStringTable.executeUpdate();
			delStringTable.close();
			PreparedStatement createStringTable = conn
					.prepareStatement("create table " + SQLDOMAIN.TABLE_STRING
							+ " (id int primary key, value varchar(50));");
			createStringTable.executeUpdate();
			createStringTable.close();
			PreparedStatement dropClassTable = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_CLASS + ";");
			dropClassTable.executeUpdate();
			dropClassTable.close();
			PreparedStatement createClassTable = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.TABLE_CLASS
							+ " (id int primary key,name varchar(50),super_class_id int,class_load_id int,"
							+ "signers_id int,protection_domain_id int,instance_size int,length int);");
			createClassTable.executeUpdate();
			createClassTable.close();
			PreparedStatement dropInstanceTable = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_INSTANCE + ";");
			dropInstanceTable.executeUpdate();
			dropInstanceTable.close();
			PreparedStatement createInstanceTable = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.TABLE_INSTANCE
							+ " (id int,class_id int,data_size int,length int);");
			createInstanceTable.executeUpdate();
			createInstanceTable.close();
			PreparedStatement dropPrimitivearrayTable = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_PRIMITIVEARRAY + ";");
			dropPrimitivearrayTable.executeUpdate();
			dropPrimitivearrayTable.close();
			PreparedStatement createPrimitiverArrayTable = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.TABLE_PRIMITIVEARRAY
							+ "(id int primary key,type int,length int);");
			createPrimitiverArrayTable.executeUpdate();
			createPrimitiverArrayTable.close();
			PreparedStatement dropObjArrayTable = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_OBJARRAY + ";");
			dropObjArrayTable.executeUpdate();
			dropObjArrayTable.close();
			PreparedStatement createObjArrayTable = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.TABLE_OBJARRAY
							+ "(id int primary key,element_class_id int,count int,length int);");
			createObjArrayTable.executeUpdate();
			createObjArrayTable.close();
			PreparedStatement dropHeapObjArrayIndex = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS + ";");
			dropHeapObjArrayIndex.executeUpdate();
			dropHeapObjArrayIndex.close();
			PreparedStatement createHeapObjArrayIndex = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
							+ "(id int,element_instance_id int,int length);");
			createHeapObjArrayIndex.executeUpdate();
			createHeapObjArrayIndex.close();
			PreparedStatement dropInstanceFiled = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD + ";");
			dropInstanceFiled.executeUpdate();
			dropInstanceFiled.close();
			PreparedStatement createInstanceField = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD
							+ "(instance_id int, type int,value,field_name)");
			createInstanceField.executeUpdate();
			createInstanceField.close();
			PreparedStatement dropClassFiled = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD + ";");
			dropClassFiled.executeUpdate();
			dropClassFiled.close();
			PreparedStatement createClassField = conn
					.prepareStatement("create table "
							+ SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD
							+ "(class_id int, type int,value,field_name)");
			createClassField.executeUpdate();
			createClassField.close();
			PreparedStatement dropRootIds = conn
					.prepareStatement("drop table if exists "
							+ SQLDOMAIN.ROOT_IDS);
			dropRootIds.execute();
			dropRootIds.close();
			PreparedStatement createRootIds = conn
					.prepareStatement("create table " + SQLDOMAIN.ROOT_IDS
							+ "(id int,type)");
			createRootIds.execute();
			createRootIds.close();
			conn.commit();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException excep) {
				}
			}
		}
	}

	/**
	 * 判断是否链接数据库
	 * 
	 * @return
	 */
	public boolean hasConnect() {
		return conn != null;
	}

	/**
	 * 查询最大内存占用块
	 * 
	 * @return
	 */
	public ArrayList<InstanceTraceItem> findHeapMax() {
		ArrayList<InstanceTraceItem> items = new ArrayList<InstanceTraceItem>();

		PreparedStatement selectMaxState;
		try {
			selectMaxState = conn
					.prepareStatement("select * from table_primitive_array order by length desc limit 10;");
			ResultSet resultSet = selectMaxState.executeQuery();
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				// traceIds.clear();
				int length = findLengthById(id, new ArrayList<Integer>());
				InstanceTraceItem item = getInstanceTraceItem(id);
				if (item != null) {
					items.add(item);
				}
				if (items.size() >= 2) {
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return items;
	}

	/**
	 * 根据instance_id查找类名
	 * 
	 * @param instance_id
	 * @return
	 */
	String getClassNameForInstance(int instance_id) {
		if (instanceToClass != null && instanceToClass.size() > 0
				&& instanceToClass.containsKey(instance_id)) {
			return getClassNameForClass(instanceToClass.get(instance_id));
		} else {
			try {
				boolean hasData = false;
				PreparedStatement statement = conn
						.prepareStatement("select * from "
								+ SQLDOMAIN.TABLE_INSTANCE + " where id = "
								+ instance_id);
				ResultSet instanceSet = statement.executeQuery();
				while (instanceSet.next()) {
					hasData = true;
					int class_id = instanceSet.getInt("class_id");
					return getClassNameForClass(class_id);
				}
				if (!hasData) {
					PreparedStatement objArrState = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_OBJARRAY + " where id = "
									+ instance_id);
					ResultSet resultset = objArrState.executeQuery();
					while (resultset.next()) {
						hasData = true;
						int class_id = resultset.getInt("element_class_id");
						return getClassNameForClass(class_id);
					}
				}
				if (!hasData) {
					PreparedStatement priArrState = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_PRIMITIVEARRAY
									+ " where id = " + instance_id);
					ResultSet resultset = priArrState.executeQuery();
					while (resultset.next()) {
						return BasicType.fromType(resultset.getInt("type"))
								.getTypeName()
								+ "["
								+ resultset.getInt("length") + "]";
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 根据类名查实例
	 * 
	 * @param name
	 * @return
	 */
	public List getInstanceForClass(String name) {
		ArrayList list = new ArrayList();
		try {
			int classId = 0;
			PreparedStatement cls = conn.prepareStatement("select id from "
					+ SQLDOMAIN.TABLE_CLASS + " where name=\"" + name + "\"");
			ResultSet set = cls.executeQuery();
			while (set.next()) {
				classId = set.getInt("id");
			}
			PreparedStatement insForCls = conn
					.prepareStatement("select id from "
							+ SQLDOMAIN.TABLE_INSTANCE + " where class_id ="
							+ classId);
			ResultSet resultSet = insForCls.executeQuery();
			while (resultSet.next()) {
				list.add(resultSet.getInt("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 根据class_id查类名
	 * 
	 * @param class_id
	 * @return
	 */
	String getClassNameForClass(int class_id) {
		try {
			if (indexClassName != null && indexClassName.size() > 0
					&& indexClassName.containsKey(class_id)) {
				return indexClassName.get(class_id);
			}
			PreparedStatement classState = conn
					.prepareStatement("select name from "
							+ SQLDOMAIN.TABLE_CLASS + " where id = " + class_id);
			ResultSet resultSet = classState.executeQuery();
			while (resultSet.next()) {
				return resultSet.getString("name");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public InstanceTraceItem getInstanceTraceItem(int id) {
		Queue<Integer> queue = new LinkedList<Integer>();
		InstanceTraceItem originItem = initedTraceItems.get(id);
		HashMap<Integer, InstanceTraceItem> tracedMap = new HashMap<Integer, InstanceTraceItem>();
		InstanceTraceItem result = new InstanceTraceItem();
		if (originItem != null) {
			result = new InstanceTraceItem();
			result.setId(originItem.getId());
			result.setName(originItem.getName());
			tracedMap.put(id, result);
			queue.offer(id);
			while (queue.size() != 0) {
				int temp_id = queue.poll();
				InstanceTraceItem tempOrigin = initedTraceItems.get(temp_id);
				InstanceTraceItem temp = tracedMap.get(temp_id);
				Set<InstanceTraceItem> traceItems = tempOrigin.getTraceItems();
				Iterator<InstanceTraceItem> iterator = traceItems.iterator();
				while (iterator.hasNext()) {
					InstanceTraceItem tempOriginUnder = iterator.next();
					if (!tracedMap.containsKey(tempOriginUnder.getId())) {
						queue.offer(tempOriginUnder.getId());
						InstanceTraceItem tempUnder = new InstanceTraceItem();
						tempUnder.setId(tempOriginUnder.getId());
						HashMap<Integer, String> filednameMap = instanceMap
								.get(temp_id);
						HashMap<Integer, String> filednameMap2 = classMap
								.get(temp_id);
						HashMap<Integer, String> filednameMap3 = arrMap
								.get(temp_id);
						if (filednameMap != null
								&& filednameMap.get(tempOriginUnder.getId()) != null) {
							String filedName = filednameMap.get(tempOriginUnder
									.getId());
							tempUnder.setFieldName(filedName);
						} else if (filednameMap2 != null
								&& filednameMap2.get(tempOriginUnder.getId()) != null) {
							String filedName = filednameMap2
									.get(tempOriginUnder.getId());
							tempUnder.setFieldName(filedName);
						} else if (filednameMap3 != null
								&& filednameMap3.get(tempOriginUnder.getId()) != null) {
							String filedName = filednameMap3.get(temp_id);
							tempUnder.setFieldName(filedName);
						}
						tempUnder.setName(tempOriginUnder.getName());
						temp.addTrace(tempUnder);
						tracedMap.put(tempUnder.getId(), tempUnder);
					}

				}
			}
		}
		// initTrace(result);
		return initTrace(result);
	}

	private InstanceTraceItem initTrace(InstanceTraceItem traceItem) {
		InstanceTraceItem temp = new InstanceTraceItem();
		Set<InstanceTraceItem> traceItems = traceItem.getTraceItems();
		temp.setId(traceItem.getId());
		temp.setName(traceItem.getName());
		temp.setFieldName(traceItem.getFieldName());
		if (traceItems.size() > 0) {
			Iterator<InstanceTraceItem> iterator = traceItems.iterator();
			while (iterator.hasNext()) {
				InstanceTraceItem item = iterator.next();
				InstanceTraceItem traceItem2 = initTrace(item);
				if (traceItem2 != null) {
					temp.addTrace(traceItem2);
				}
			}
			if (temp.getTraceItems().size() == 0
					&& !gcRoot.containsKey(temp.getId())) {
				return null;
			}
		} else if (!gcRoot.containsKey(temp.getId())) {
			return null;
		}
		if (temp.getTraceItems().size() == 0
				&& !gcRoot.containsKey(temp.getId())) {
			return null;
		}
		return temp;
	}


	/**
	 * @param classId
	 * @return
	 */
	private int getClassLength(int classId) {
		int length = 0;
		try {
			PreparedStatement classInfo = conn
					.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS
							+ " where id = " + classId);
			ResultSet set = classInfo.executeQuery();
			while (set.next()) {
				length = set.getInt("length");
				if (length > 0) {
					break;
				} else {
					if (classLength.get(classId) != null)
						length += classLength.get(classId);
					if (set.getInt("super_class_id") != 0)
						length += getClassLength(set.getInt("super_class_id"));
					PreparedStatement update = conn.prepareStatement("update "
							+ SQLDOMAIN.TABLE_CLASS + " set length=" + length
							+ " where id = " + classId);
					update.executeUpdate();
					update.close();
				}
			}
			classInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return length;
	}

	/**
	 * 
	 */
	public void initClassLength() {
		PreparedStatement classInfo = null;
		try {
			classInfo = conn.prepareStatement("select * from "
					+ SQLDOMAIN.TABLE_CLASS);
			ResultSet set = classInfo.executeQuery();
			while (set.next()) {
				int id = set.getInt("id");
				getClassLength(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	public double getTotalSize() {
		if (totalSize > 0) {
			return totalSize;
		}
		try {
			PreparedStatement calculatePrimitiveSize = conn
					.prepareStatement("select length from "
							+ SQLDOMAIN.TABLE_PRIMITIVEARRAY
							+ " where length >0");
			ResultSet primitiveResult = calculatePrimitiveSize.executeQuery();
			while (primitiveResult.next()) {
				totalSize += primitiveResult.getInt("length");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totalSize;
	}

	private int initInstanceLength(int id) {
		int length = 0;
		try {
			boolean hasInstance = false;
			PreparedStatement instanceState = conn
					.prepareStatement(new StringBuilder("select * from ")
							.append(SQLDOMAIN.TABLE_INSTANCE)
							.append(" where id = ").append(id).append(";")
							.toString());
			ResultSet instanceResult = instanceState.executeQuery();
			while (instanceResult.next()) {
				hasInstance = true;
				int classId = instanceResult.getInt("class_id");
				length += instanceResult.getInt("length");
				if (length > 0) {
					return length;
				}
				List<InstanceField> fields = classFiled.get(classId);
				// length+=classLength.get(classId);
				if (length >= 0) {
					instanceState.close();
					instanceResult.close();
					return length;
				}
				Iterator<InstanceField> iterator = fields.iterator();
				while (iterator.hasNext()) {
					boolean isGetSubInstance = false;
					InstanceField field = iterator.next();
					if (field.getType().type != 2) {
						length += field.getType().size;
					} else if (!isGetSubInstance) {
						isGetSubInstance = true;
						PreparedStatement subInstance = conn
								.prepareStatement(new StringBuilder(
										"select value from ")
										.append(SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD)
										.append(" where instance_id =")
										.append(id).append(";").toString());
						ResultSet subInstanceSet = subInstance.executeQuery();
						if (!subInstanceSet.next()) {
							PreparedStatement subObjArrayState = conn
									.prepareStatement(new StringBuilder(
											"select length from ")
											.append(SQLDOMAIN.TABLE_OBJARRAY)
											.append(" where id = ").append(id)
											.append(";").toString());
							ResultSet subObjArraySet = subObjArrayState
									.executeQuery();
							if (!subObjArraySet.next()) {
								PreparedStatement subPrimitiveArrayState = conn
										.prepareStatement(new StringBuilder(
												"select * from ")
												.append(SQLDOMAIN.TABLE_PRIMITIVEARRAY)
												.append(" where id = ")
												.append(id).append(";")
												.toString());
								ResultSet subPriArraySet = subPrimitiveArrayState
										.executeQuery();
								while (subPriArraySet.next()) {
									int type = subPriArraySet.getInt("type");
									int count = subPriArraySet.getInt("count");
									BasicType basicType = BasicType
											.fromType(type);
									length += basicType.size * count;
								}
								subPrimitiveArrayState.close();
								subPriArraySet.close();
							} else {
								length += subObjArraySet.getInt("length");
							}
							subObjArrayState.close();
							subObjArraySet.close();
						} else {
							length += initInstanceLength(subInstanceSet
									.getInt("value"));
							while (subInstanceSet.next()) {
								length += initInstanceLength(subInstanceSet
										.getInt("value"));
							}
						}

					}
				}
			}
			if (!hasInstance) {

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return length;
	}

	/**
	 * dump文件中的标签解析后的处理
	 * 
	 * @param tag
	 *            heap标签
	 * @param obj
	 *            解析得出的结果
	 */
	public void insertHeapData(int tag, Object obj) {
		try {
			int id;
			StringBuilder buffer = new StringBuilder();
			PreparedStatement insertState = null;
			int i;
			switch (tag) {
			case HeapTag.ROOT_UNKNOWN:
				id = (int) obj;
				gcRoot.put(id, "unknow root");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"unknow root\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_JNI_GLOBAL:
				id = ((GlobalModel) obj).getId();
				gcRoot.put(id, "global property");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"global property\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_JNI_LOCAL:
				id = ((LocalModel) obj).getId();
				gcRoot.put(id, "local property");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"local property\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_JAVA_FRAME:
				id = ((JavaFrameModel) obj).getId();
				gcRoot.put(id, "java frame");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"java frame\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_NATIVE_STACK:
				id = ((NativeStackModel) obj).getId();
				gcRoot.put(id, "native stack");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"native stack\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_STICKY_CLASS:
				id = (int) obj;
				gcRoot.put(id, "system class");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"system class\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_THREAD_BLOCK:
				id = ((ThreadBlock) obj).getId();
				gcRoot.put(id, "thread block");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"thread block\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_MONITOR_USED:
				id = (int) obj;
				gcRoot.put(id, "monistor");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"monistor\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.ROOT_THREAD_OBJECT:
				id = ((ThreadObjectModel) obj).getId();
				gcRoot.put(id, "thread");
				insertState = conn.prepareStatement("insert into "
						+ SQLDOMAIN.ROOT_IDS + " values(" + id + ","
						+ "\"thread\"" + ");");
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.INSTANCE_DUMP:
				Instance instance = (Instance) obj;
				instances.put(instance.getObjectId(), instance);
				int instanceLength = 0;
				if (referClasses.contains(instance.getClassId())) {
					finalizersInstances.put(instance.getObjectId(), instance);
				}
				buffer.append("insert into ").append(SQLDOMAIN.TABLE_INSTANCE)
						.append(" values (").append(instance.getObjectId())
						.append(",").append(instance.getClassId()).append(",")
						.append(instance.getLength()).append(",")
						.append(instanceLength).append(");");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				insertState.close();
				break;
			case HeapTag.PRIMITIVE_ARRAY_DUMP:
				buffer.append("insert into ")
						.append(SQLDOMAIN.TABLE_PRIMITIVEARRAY)
						.append(" values(")
						.append(((PrimitiveArray) obj).getObjectId())
						.append(",")
						.append(((PrimitiveArray) obj).getType().type)
						.append(",")
						.append(((PrimitiveArray) obj).getArrayData().length)
						.append(");");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				if (insertState != null) {
					insertState.close();
				}
				break;
			case HeapTag.OBJECT_ARRAY_DUMP:
				ObjectArray objectArray = (ObjectArray) obj;
				int num = objectArray.getCount();
				for (i = 0; i < num && objectArray.getElements()[i] != 0; i++) {
					StringBuilder buffer1 = new StringBuilder("insert into ")
							.append(SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS)
							.append(" values(")
							.append(objectArray.getObjectId()).append(",")
							.append(objectArray.getElements()[i]).append(",")
							.append(0).append(");");
					PreparedStatement statement = conn.prepareStatement(buffer1
							.toString());
					statement.executeUpdate();
					objArr.put(objectArray.getObjectId(), objectArray);

				}
				buffer.append("insert into ").append(SQLDOMAIN.TABLE_OBJARRAY)
						.append(" values(").append(objectArray.getObjectId())
						.append(",").append(objectArray.getElementClassId())
						.append(",").append(objectArray.getCount()).append(",")
						.append(0).append(");");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				if (insertState != null) {
					insertState.close();
				}
				break;
			case HeapTag.CLASS_DUMP:
				PreparedStatement select = conn
						.prepareStatement(new StringBuilder("select * from ")
								.append(SQLDOMAIN.TABLE_STRING)
								.append(" where id=")
								.append(((ClassDefinition) obj)
										.getNameStringId()).append(";")
								.toString());
				ResultSet rs = select.executeQuery();
				String name = rs.getString("value");

				ClassDefinition temp = (ClassDefinition) obj;
				if (name.contains("java.lang.ref.")) {
					referClasses.add(temp.getObjectId());
				}
				int length1 = 0;
				List<InstanceField> list = new ArrayList<InstanceField>();
				list.addAll(temp.getInstanceFields());
				if (temp.getSuperClassObjectId() != 0
						&& classFiled.get(temp.getSuperClassObjectId()) != null)
					list.addAll(classFiled.get(temp.getSuperClassObjectId()));
				classFiled.put(temp.getObjectId(), list);
				Iterator<ConstantField> constantFieldIterator = temp
						.getConstantFields().iterator();
				Iterator<StaticField> staticFieldIterator = temp
						.getStaticFields().iterator();
				int cl = 0;
				i = 0;
				while (constantFieldIterator.hasNext()) {
					ConstantField constantField = constantFieldIterator.next();
					int type = (Integer) constantField.getType().type;
					if (type == 2) {
						int value = Utils.byteArrayToInt(
								constantField.getValue(), i);
						if (value != 0) {
							// classStaticVariMap.put(value,
							// temp.getObjectId(),);
							instanceRefMap.add(new IndexMap(temp.getObjectId(),
									value, "unknow", IndexMap.TYPE_CLASS));
							PreparedStatement statement = conn
									.prepareStatement(new StringBuilder(
											"insert into ")
											.append(SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD)
											.append(" (class_id,type,value) values (")
											.append(temp.getObjectId())
											.append(",").append(type)
											.append(",").append("?")
											.append(");").toString());
							statement.setInt(1, value);
							statement.executeUpdate();
							statement.close();
						}
					} else {
						cl += constantField.getValue().length;
					}
				}
				while (staticFieldIterator.hasNext()) {
					StaticField staticField = staticFieldIterator.next();
					int type = staticField.getType().type;
					if (type == 2) {
						int value = Utils.byteArrayToInt(
								staticField.getValue(), i);
						// gcRootIds.add(value);
						id = staticField.getFieldNameId();

						PreparedStatement stringStatement = conn
								.prepareStatement(new StringBuilder(
										"select * from ")
										.append(SQLDOMAIN.TABLE_STRING)
										.append(" where id=").append(id)
										.append(";").toString());
						ResultSet stringSet = stringStatement.executeQuery();
						String fieldName = null;
						if (stringSet.next()) {
							fieldName = stringSet.getString("value");
						}
						// classMap.put(value, temp.getObjectId(),
						// fieldName);
						stringStatement.close();
						stringSet.close();
						if (value != 0) {
							// instanceRefMap.add(new
							// IndexMap(temp.getObjectId(),
							// value, fieldName, IndexMap.TYPE_CLASS));
							rootClassMap.put(temp.getObjectId(), value,
									fieldName);
							PreparedStatement statement = conn
									.prepareStatement(new StringBuilder(
											"insert into ")
											.append(SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD)
											.append(" (class_id,type,value,field_name) values (")
											.append(temp.getObjectId())
											.append(",").append(type)
											.append(",").append("?")
											.append(",").append("?")
											.append(");").toString());
							statement.setInt(1, value);
							statement.setString(2, fieldName);
							statement.executeUpdate();
							statement.close();
						} else {
							cl += staticField.getValue().length;
						}
					}
					classLength.put(temp.getObjectId(), cl);
				}
				indexClassName.put(temp.getObjectId(), name);
				buffer.append("insert into ").append(SQLDOMAIN.TABLE_CLASS)
						.append(" values (").append(temp.getObjectId())
						.append(",").append("'").append(name).append("'")
						.append(",").append(temp.getSuperClassObjectId())
						.append(",").append(temp.getClassLoaderObjectId())
						.append(",").append(temp.getSignersObjectId())
						.append(",").append(temp.getProtectionDomainObjectId())
						.append(",").append(temp.getInstanceSize()).append(",")
						.append(temp.getLength()).append(");");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				if (insertState != null) {
					insertState.close();
				}
				select.close();
				rs.close();
				break;
			}
		} catch (SQLException e) {
			// TODO: handle exception
			// e.printStackTrace();
		}

	}

	/**
	 * @param classId
	 * @param traceIds
	 * @return
	 * @throws SQLException
	 */
	public int findLengthById(int classId, List<Integer> traceIds)
			throws SQLException {
		int length = 0;
		traceIds.add(classId);
		// level++;
		PreparedStatement cls = conn.prepareStatement("select * from "
				+ SQLDOMAIN.TABLE_CLASS + " where id = " + classId);
		ResultSet clsSet = cls.executeQuery();
		boolean hasData = false;
		while (clsSet.next()) {
			hasData = true;
			length += clsSet.getInt("length");
			PreparedStatement insForClass = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.TABLE_INSTANCE + " where class_id="
							+ classId);
			ResultSet instances = insForClass.executeQuery();
			while (instances.next()) {
				int id = instances.getInt("id");
				length += findLengthById(id, traceIds);
			}
			insForClass.close();
			instances.close();
			PreparedStatement staticAndConsForClass = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD
							+ " where class_id=" + classId);
			ResultSet sAndCSet = staticAndConsForClass.executeQuery();
			while (sAndCSet.next()) {
				int id = sAndCSet.getInt("value");
				length += findLengthById(id, traceIds);
			}
			staticAndConsForClass.close();
			sAndCSet.close();
		}
		cls.close();
		clsSet.close();
		if (!hasData) {
			PreparedStatement instance = conn.prepareStatement("select * from "
					+ SQLDOMAIN.TABLE_INSTANCE + " where id = " + classId);
			ResultSet insInfo = instance.executeQuery();
			while (insInfo.next()) {
				hasData = true;
				PreparedStatement insForClass = conn
						.prepareStatement("select * from "
								+ SQLDOMAIN.TABLE_CLASS + " where id = "
								+ insInfo.getInt("class_id"));
				ResultSet inForClassSet = insForClass.executeQuery();
				length += insInfo.getInt("length");
				PreparedStatement insForIns = conn
						.prepareStatement("select * from "
								+ SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD
								+ " where instance_id=" + classId);
				ResultSet insSet = insForIns.executeQuery();
				while (insSet.next()) {
					int id = insSet.getInt("value");
					if (traceIds.contains(id)) {
						return length;
					} else {
						length += findLengthById(id, traceIds);
					}
				}
				insForIns.close();
				insSet.close();
				insForClass.close();
				inForClassSet.close();
			}
			instance.close();
			insInfo.close();
			if (!hasData) {
				PreparedStatement objArrState = conn
						.prepareStatement("select * from "
								+ SQLDOMAIN.TABLE_OBJARRAY + " where id = "
								+ classId);
				ResultSet objSet = objArrState.executeQuery();
				while (objSet.next()) {
					hasData = true;
					PreparedStatement objForClass = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_CLASS + " where id = "
									+ objSet.getInt("element_class_id"));
					ResultSet objForClassSet = objForClass.executeQuery();
					PreparedStatement objIndexState = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
									+ " where id=" + classId);
					ResultSet objIndexSet = objIndexState.executeQuery();
					while (objIndexSet.next()) {
						length += findLengthById(
								objIndexSet.getInt("element_instance_id"),
								traceIds);
					}
					objForClass.close();
					objForClassSet.close();
				}
				objArrState.close();
				objSet.close();
				if (!hasData) {
					hasData = true;
					PreparedStatement priArrState = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_PRIMITIVEARRAY
									+ " where id = " + classId);
					ResultSet set = priArrState.executeQuery();
					while (set.next()) {
						length += set.getInt("length");
					}
					priArrState.close();
					set.close();
				}
			}

		}

		return length;
	}

	/**
	 * @param classId
	 * @param traceIds
	 * @param originLength
	 * @return
	 */
	private int findLengthById(int classId, List<Integer> traceIds,
			int originLength) {
		int length = originLength;
		traceIds.add(classId);

		try {
			PreparedStatement cls = conn.prepareStatement("select * from "
					+ SQLDOMAIN.TABLE_CLASS + " where id = " + classId);
			ResultSet clsSet = cls.executeQuery();
			boolean hasData = false;
			while (clsSet.next()) {
				hasData = true;
				length += clsSet.getInt("length");
				if (originLength > 0) {
					return length;
				}
			}
			cls.close();
			clsSet.close();
			if (!hasData) {
				PreparedStatement instance = conn
						.prepareStatement("select * from "
								+ SQLDOMAIN.TABLE_INSTANCE + " where id = "
								+ classId);
				ResultSet insInfo = instance.executeQuery();
				while (insInfo.next()) {
					hasData = true;
					PreparedStatement insForClass = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_CLASS + " where id = "
									+ insInfo.getInt("class_id"));
					ResultSet inForClassSet = insForClass.executeQuery();
					length += insInfo.getInt("length");
					if (originLength > 0) {
						return length;
					}
					insForClass.close();
					inForClassSet.close();
				}
				instance.close();
				insInfo.close();
				if (!hasData) {
					PreparedStatement objArrState = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.TABLE_OBJARRAY + " where id = "
									+ classId);
					ResultSet objSet = objArrState.executeQuery();
					while (objSet.next()) {
						hasData = true;
						PreparedStatement objForClass = conn
								.prepareStatement("select * from "
										+ SQLDOMAIN.TABLE_CLASS
										+ " where id = "
										+ objSet.getInt("element_class_id"));
						ResultSet objForClassSet = objForClass.executeQuery();
						PreparedStatement objIndexState = conn
								.prepareStatement("select * from "
										+ SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
										+ " where id=" + classId);
						ResultSet objIndexSet = objIndexState.executeQuery();
						while (objIndexSet.next()) {
							length += findLengthById(
									objIndexSet.getInt("element_instance_id"),
									traceIds);
						}
						objForClass.close();
						objForClassSet.close();
					}
					objArrState.close();
					objSet.close();
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return length;
	}

	/**
	 * 初始化整个从gcroot出发的引用表并插入数据库
	 */
	public void initInstanceOOM() {
		HashMap<Integer, Instance> gcLinkInstance = initGCLinkInstance(gcRoot,
				instances);
		initInstanceDB(instanceRefMap);
	}


	private HashMap<Integer, Instance> initGCLinkInstance(
			Hashtable<Integer, String> origin, HashMap<Integer, Instance> all) {
		HashMap<Integer, Instance> result = new HashMap<Integer, Instance>();
		Iterator iterator = origin.keySet().iterator();
		while (iterator.hasNext()) {
			int key = (int) iterator.next();
			initInstanceTrace(key, all, new ArrayList<Integer>());
		}
		Iterator<Integer> iterator2 = rootClassMap.keySet().iterator();
		while (iterator2.hasNext()) {
			int classId = iterator2.next();
			if (gcRoot.containsKey(classId)) {
				HashMap<Integer, String> temp = rootClassMap.get(classId);
				Iterator iterator3 = temp.keySet().iterator();
				while (iterator3.hasNext()) {
					int instanceId = (int) iterator3.next();
					String fieldname = temp.get(instanceId);
					instanceRefMap.add(new IndexMap(classId, instanceId,
							fieldname, IndexMap.TYPE_CLASS));
					initInstanceTrace(instanceId, all, new ArrayList<Integer>());

				}
			}
		}
		Iterator<Integer> iterator3 = objArr.keySet().iterator();
		while (iterator3.hasNext()) {
			int objId = iterator3.next();
			if (rootObjArr.containsKey(objId)) {
				ObjectArray temp = rootObjArr.get(objId);
				for (int i = 0; i < temp.getElements().length
						&& temp.getElements()[i] != 0; i++) {
					if (instances.containsKey(temp.getElements()[i])) {
						instanceRefMap.add(new IndexMap(temp.getObjectId(),
								temp.getElements()[i], "[" + i + "]",
								IndexMap.TYPE_ARR));
						initInstanceTrace(temp.getElements()[i], all,
								new ArrayList<Integer>());
					}

				}
			}
		}
		return result;

	}

	/**
	 * @param id
	 *            需要分析的instanceId
	 * @param all
	 *            instanc表
	 * @param trace
	 *            已初始化过的路径
	 */
	private void initInstanceTrace(int id, HashMap<Integer, Instance> all,
			ArrayList<Integer> trace) {
		if (trace.contains(id)) {
			return;
		}
		if (finalizersInstances.containsKey(id)) {
			return;
		}
		Instance instance = all.get(id);

		if (instance != null) {
			if (gcRootInstance.containsKey(id)) {
				return;
			} else {
				gcRootInstance.put(id, instance);
			}
			List<InstanceField> instanceFields = classFiled.get(instance
					.getClassId());
			Iterator<InstanceField> fieldIterator = instanceFields.iterator();
			ArrayList types = new ArrayList();
			ArrayList sizes = new ArrayList();
			ArrayList<String> fieldNames = new ArrayList<String>();
			while (fieldIterator.hasNext()) {
				InstanceField field = fieldIterator.next();
				BasicType type = field.getType();
				int StringId = field.getFieldNameId();
				PreparedStatement stringStatement;
				try {
					stringStatement = conn.prepareStatement(new StringBuilder(
							"select * from ").append(SQLDOMAIN.TABLE_STRING)
							.append(" where id=").append(StringId).append(";")
							.toString());
					ResultSet stringSet = stringStatement.executeQuery();
					if (stringSet.next()) {
						fieldNames.add(stringSet.getString("value"));
					}
					stringStatement.close();
					stringSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				types.add(type.type);
				sizes.add(type.size);
			}
			Iterator typeIterator = types.iterator();
			Iterator iterator1 = sizes.iterator();
			Iterator nameIterator = fieldNames.iterator();
			byte[] bytes = instance.getInstanceFieldData();
			int i = 0;
			while (i < bytes.length && typeIterator.hasNext()) {
				int typeItem = (Integer) typeIterator.next();
				int size = (Integer) iterator1.next();
				String name = (String) nameIterator.next();
				if (typeItem == 2) {
					int value = Utils.byteArrayToInt(bytes, i);
					if (value != 0 && all.containsKey(value)) {
						instanceRefMap.add(new IndexMap(id, value, name,
								IndexMap.TYPE_INSTANCE));
						if (!gcRootInstance.containsKey(value)) {
							initInstanceTrace(value, all, trace);
						}
					} else if (value != 0 && objArr.containsKey(value)
							&& !rootObjArr.containsKey(value)) {
						rootObjArr.put(value, objArr.get(value));
						instanceRefMap.add(new IndexMap(id, value, name,
								IndexMap.TYPE_INSTANCE));
					}
				}
				i = i + size;
			}
		} else if (objArr.containsKey(id)) {
			rootObjArr.put(id, objArr.get(id));
		}
	}

	/**
	 * 初始化数据库中相关的映射表
	 * 
	 * @param temp映射表
	 */
	private void initInstanceDB(HashSet<IndexMap> temp) {
		// Iterator iterator = oomMap.keySet().iterator();
		Iterator<IndexMap> iterator = temp.iterator();
		initMaps(temp);
		initGCMap();
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				initGCMap();
//			}
//		}).start();
		while (iterator.hasNext()) {

			IndexMap map = iterator.next();
			// if (map.fieldname.equals("mAttachInfo")) {
			// continue;
			// }
			PreparedStatement statement;
			try {
				statement = conn
						.prepareStatement(new StringBuilder("insert into ")
								.append(SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD)
								.append(" (instance_id,type,value,field_name) values (")
								.append(map.key).append(",").append(map.type)
								.append(",").append(map.value).append(",")
								.append("?").append(");").toString());
				statement.setString(1, map.fieldname);
				statement.executeUpdate();
				statement.close();
				// instanceMap.put(map.value, map.key, map.fieldname);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initMaps(HashSet<IndexMap> temp) {
		Iterator<IndexMap> iterator = temp.iterator();
		while (iterator.hasNext()) {

			IndexMap map = iterator.next();
			if (map.fieldname.equals("mAttachInfo")) {
				continue;
			}

			int type = map.type;
			switch (type) {
			case IndexMap.TYPE_INSTANCE:
				instanceMap.put(map.value, map.key, map.fieldname);
				mapInstance.put(map.key, map.value, map.fieldname);
				break;

			case IndexMap.TYPE_ARR:
				arrMap.put(map.value, map.key, map.fieldname);
				mapArr.put(map.key, map.value, map.fieldname);
				break;
			case IndexMap.TYPE_CLASS:
				classMap.put(map.value, map.key, map.fieldname);
				mapClass.put(map.key, map.value, map.fieldname);
				break;

			default:
				break;
			}
		}
	}

	private synchronized void initIndex(int id, InstanceTraceItem item,
			Hashtable<Integer, Integer> traceIds, int rootId, int path) {
		if (mapInstance.containsKey(id)) {
			HashMap<Integer, String> itemHashMap = mapInstance.get(id);
			Iterator iter = itemHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				int temp_id = (int) entry.getKey();
				if (!gcRoot.contains(temp_id)
						&& (instances.containsKey(temp_id) || objArr
								.containsKey(temp_id))) {
					if (!initedTraceItems.containsKey(temp_id)) {
						int tempPath = path + 1;
						traceIds.put(temp_id, tempPath);
						String value = (String) entry.getValue();
						InstanceTraceItem temp = new InstanceTraceItem();
						temp.setId(temp_id);
						item.setFieldName(value);
						temp.setName(getClassNameForInstance(temp_id));
						temp.addTrace(item, rootId, temp_id);
						initedTraceItems.put(temp_id, temp);
						initIndex(temp_id, temp, traceIds, rootId, tempPath);
					} else if (traceIds.containsKey(temp_id)) {
						int tempPath = path + 1;
						int pathLen = item.getPathLen(rootId);
						int len = traceIds.get(temp_id);
						if (pathLen != -1 && pathLen + 1 < len) {
							InstanceTraceItem temp = initedTraceItems
									.get(temp_id);
							temp.addTrace(item, rootId, tempPath);
						}
					} else {
						int tempPath = path + 1;
						InstanceTraceItem temp = initedTraceItems.get(temp_id);
						temp.addTrace(item, rootId, tempPath);
						traceIds.put(temp_id, path);
						initIndex(temp_id, temp, traceIds, rootId, tempPath);
						// initIndex(temp_id, temp, traceIds, rootId, tempPath);
					}
				}
			}
		} else if (mapClass.containsKey(id)) {
			HashMap<Integer, String> itemHashMap = mapClass.get(id);
			Iterator iter = itemHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				int temp_id = (int) entry.getKey();
				if (!gcRoot.contains(temp_id) && instances.containsKey(temp_id)) {
					int tempPath = path + 1;
					if (!initedTraceItems.containsKey(temp_id)) {
						traceIds.put(temp_id, temp_id);
						String value = (String) entry.getValue();
						InstanceTraceItem temp = new InstanceTraceItem();
						temp.setId(temp_id);
						item.setFieldName(value);
						temp.setName(getClassNameForInstance(temp_id));
						temp.addTrace(item, rootId, tempPath);
						initedTraceItems.put(temp_id, temp);
						initIndex(temp_id, temp, traceIds, rootId, tempPath);
					} else if (!traceIds.contains(temp_id)) {
						InstanceTraceItem temp = initedTraceItems.get(temp_id);
						temp.addTrace(item, rootId, tempPath);
						traceIds.put(temp_id, path);
						initIndex(temp_id, temp, traceIds, rootId, tempPath);
					} else {
						InstanceTraceItem temp = initedTraceItems.get(temp_id);
						temp.addTrace(item, rootId, tempPath);
						traceIds.put(temp_id, path);
					}
				}
			}
		} else if (mapArr.containsKey(id)) {
			HashMap<Integer, String> itemHashMap = mapArr.get(id);
			Iterator iter = itemHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				int temp_id = (int) entry.getKey();
				if (!gcRoot.contains(temp_id) && instances.containsKey(temp_id)) {
					int tempPath = path + 1;
					if (!initedTraceItems.containsKey(temp_id)) {
						traceIds.put(temp_id, tempPath);
						String value = (String) entry.getValue();
						InstanceTraceItem temp = new InstanceTraceItem();
						temp.setId(temp_id);
						item.setFieldName(value);
						temp.setName(getClassNameForInstance(temp_id));
						temp.addTrace(item, rootId, path);
						initedTraceItems.put(temp_id, temp);
						initIndex(temp_id, temp, traceIds, rootId, tempPath);
						// traceIds.remove((Integer) temp_id);
					} else if (!traceIds.contains(temp_id)) {
						InstanceTraceItem temp = initedTraceItems.get(temp_id);
						temp.addTrace(item, rootId, tempPath);
						traceIds.put(temp_id, tempPath);
						initIndex(temp_id, temp, traceIds, rootId, tempPath);
					} else {
						int pathLen = item.getPathLen(rootId);
						int len = traceIds.get(temp_id);
						if (pathLen != -1 && pathLen + 1 < len) {
							InstanceTraceItem temp = initedTraceItems
									.get(temp_id);
							temp.addTrace(item, rootId, tempPath);
						}
					}
				}
			}
		}
	}

	private void initGCMap() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
		Iterator iterator = gcRoot.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			int temp_id = (int) entry.getKey();
			if (temp_id > 10000) {
				fixedThreadPool.execute(new Runnable() {

					@Override
					public void run() {
//						// TODO Auto-generated method stub
						if (instances.containsKey(temp_id)) {
							String value = (String) entry.getValue();
							InstanceTraceItem temp = new InstanceTraceItem();
							temp.setId(temp_id);
							// temp.setFieldName(value);
							temp.setName(getClassNameForInstance(temp_id)
									+ " root:" + value);
							initedTraceItems.put(temp_id, temp);
							Hashtable<Integer, Integer> IdToPath = new Hashtable<Integer, Integer>();
							IdToPath.put(temp_id, 0);
							initIndex(temp_id, temp, IdToPath, temp_id, 0);
						} else if (indexClassName.containsKey(temp_id)) {							
							String value = (String) entry.getValue();
							InstanceTraceItem temp = new InstanceTraceItem();
							temp.setId(temp_id);
							temp.setName(indexClassName.get(temp_id) + " root:"
									+ value);
							initedTraceItems.put(temp_id, temp);
							Hashtable<Integer, Integer> IdToPath = new Hashtable<Integer, Integer>();
							IdToPath.put(temp_id, 0);
							initIndex(temp_id, temp, IdToPath, temp_id, 0);
						}
					}
				});

			}

		}
	}

}
