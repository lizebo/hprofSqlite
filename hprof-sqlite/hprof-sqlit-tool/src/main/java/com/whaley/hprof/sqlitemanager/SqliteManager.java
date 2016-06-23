package com.whaley.hprof.sqlitemanager;

import com.badoo.hprof.library.Tag;
import com.badoo.hprof.library.heap.HeapTag;
import com.badoo.hprof.library.model.*;
import com.google.common.collect.Multiset.Entry;
import com.sun.org.apache.regexp.internal.recompile;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hc on 2016/5/20.
 */
public class SqliteManager {
	private static SqliteManager instance = null;
	Connection conn = null;
	private int totalSize = 0;
	private IndexHashMap indexValueToId;
	private HashMap<Integer, String> indexClassName;
	private int classLoaderId;
	private int classLoaderInstance;

	private HashMap<Integer, List<InstanceField>> classFiled;
	private HashMap<Integer, List<ConstantField>> classConstantFiled;
	private HashMap<Integer, List<StaticField>> classStaticFiled;
	private HashMap<Integer, Integer> classLength;
	private ArrayList<Integer> systemClass;
	private HashMap<Integer, Integer> instanceToClass;
	private HashMap<Integer, String> classForName;
	private HashMap<Integer, Instance> finalizersInstances;
	private HashMap<Integer, Instance> instances;
	private HashMap<Integer, Instance> gcRootInstanceOrigin;
	private HashSet<Integer> gcRootIds;
	private HashMap<Integer, Integer> classToClassLoader;
	private HashMap<Integer, Instance> gcRootInstance;

	private SqliteManager() {
		classFiled = new HashMap<Integer, List<InstanceField>>();
		classConstantFiled = new HashMap<Integer, List<ConstantField>>();
		classStaticFiled = new HashMap<Integer, List<StaticField>>();
		classLength = new HashMap<Integer, Integer>();
		indexValueToId = new IndexHashMap();
		indexClassName = new HashMap<Integer, String>();
		systemClass = new ArrayList<Integer>();
		instanceToClass = new HashMap<Integer, Integer>();
		classForName = new HashMap<Integer, String>();
		finalizersInstances = new HashMap<Integer, Instance>();
		// oomInstance = new HashMap<Integer, Instance>();
		// oomInstance2 = new HashMap<Integer, Instance>();
		// unOomInstance = new HashMap<Integer, Instance>();
		// systemInstance = new HashMap<Integer, Instance>();
		gcRootInstanceOrigin = new HashMap<Integer, Instance>();
		gcRootIds = new HashSet<Integer>();
		instances = new HashMap<Integer, Instance>();
		classToClassLoader = new HashMap<Integer, Integer>();
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
			case HeapTag.CLASS_DUMP:
				StringBuilder delete = new StringBuilder("DELETE FROM ")
						.append(SQLDOMAIN.TABLE_CLASS).append(" WHERE id=")
						.append(((ClassDefinition) obj).getObjectId())
						.append(";");
				PreparedStatement deleteState = conn.prepareStatement(delete
						.toString());
				deleteState.executeUpdate();
				deleteState.close();
			case Tag.LOAD_CLASS:
				PreparedStatement select = conn
						.prepareStatement(new StringBuilder("select * from ")
								.append(SQLDOMAIN.TABLE_STRING)
								.append(" where id=")
								.append(((ClassDefinition) obj)
										.getNameStringId()).append(";")
								.toString());
				ResultSet rs = select.executeQuery();
				ClassDefinition temp = (ClassDefinition) obj;
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
						gcRootIds.add(value);
						if (value != 0) {
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
						gcRootIds.add(value);
						int id = staticField.getFieldNameId();

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
						stringStatement.close();
						stringSet.close();
						if (value != 0) {
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
							indexValueToId.put(value, temp.getObjectId(),
									fieldName);
						} else {
							cl += staticField.getValue().length;
						}
					}
					classLength.put(temp.getObjectId(), cl);
				}
				// if (rs.getString("value").contains("java.lang.ref."))
				// referenceClass.add(temp.getObjectId());
				if (rs.getString("value").equals(
						"dalvik.system.PathClassLoader")) {
					classLoaderId = temp.getObjectId();
				}
				if (temp.getClassLoaderObjectId() != 0
						&& tag == HeapTag.CLASS_DUMP) {
					classToClassLoader.put(temp.getObjectId(),
							temp.getClassLoaderObjectId());
				}
				if (temp.getClassLoaderObjectId() == 0
						&& tag == HeapTag.CLASS_DUMP) {
					systemClass.add(temp.getObjectId());
				}
				indexClassName.put(temp.getObjectId(), rs.getString("value"));
				buffer.append("insert into ").append(SQLDOMAIN.TABLE_CLASS)
						.append(" values (").append(temp.getObjectId())
						.append(",").append("'").append(rs.getString("value"))
						.append("'").append(",")
						.append(temp.getSuperClassObjectId()).append(",")
						.append(temp.getClassLoaderObjectId()).append(",")
						.append(temp.getSignersObjectId()).append(",")
						.append(temp.getProtectionDomainObjectId()).append(",")
						.append(temp.getInstanceSize()).append(",")
						.append(temp.getLength()).append(");");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				if (insertState != null) {
					insertState.close();
				}
				select.close();
				rs.close();
				break;
			case HeapTag.INSTANCE_DUMP:
				Instance instance = (Instance) obj;
				instances.put(instance.getObjectId(), instance);
				if (!systemClass.contains(instance.getClassId())) {
					gcRootInstanceOrigin.put(instance.getObjectId(), instance);
				}
				int instanceLength = 0;
				// if (gcRootIds.contains(instance.getObjectId())) {
				// gcRootInstance.put(instance.getObjectId(), instance);
				// }
				// if (referenceClass.contains(instance.getClassId())) {
				// break;
				// }
				if (instance.getClassId() == classLoaderId) {
					classLoaderInstance = instance.getObjectId();
				}
				buffer.append("insert into ").append(SQLDOMAIN.TABLE_INSTANCE)
						.append(" values (").append(instance.getObjectId())
						.append(",").append(instance.getClassId()).append(",")
						.append(instance.getLength()).append(",")
						.append(instanceLength).append(");");
				insertState = conn.prepareStatement(buffer.toString());
				insertState.executeUpdate();
				insertState.close();
				// if (classFiled.containsKey(instance.getClassId())) {
				// List<InstanceField> list1 = classFiled.get(instance
				// .getClassId());
				// Iterator<InstanceField> iterator2 = list1.iterator();
				// ArrayList types = new ArrayList();
				// ArrayList sizes = new ArrayList();
				// while (iterator2.hasNext()) {
				// InstanceField field = iterator2.next();
				// BasicType type = field.getType();
				// types.add(type.type);
				// sizes.add(type.size);
				// }
				// if (types.contains(2)) {
				// if
				// (systemClass.contains(instance.getClassId())&&!gcRootIds.contains(instance.getObjectId()))
				// {
				// gcRootIds.add(instance.getObjectId());
				// }
				// }
				// Iterator typeIterator = types.iterator();
				// Iterator iterator1 = sizes.iterator();
				// byte[] bytes = instance.getInstanceFieldData();
				// i = 0;
				// while (i < bytes.length && typeIterator.hasNext()) {
				// int typeItem = (Integer) typeIterator.next();
				// int size = (Integer) iterator1.next();
				// if (typeItem == 2) {
				// int value = Utils.byteArrayToInt(bytes, i);
				// if (value != 0) {
				// if
				// (gcRootIds.contains(instance.getObjectId())&&!gcRootIds.contains(value))
				// {
				// gcRootIds.add(value);
				// }
				// }
				// }
				// i = i + size;
				// }
				//
				// }

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
					if (gcRootIds.contains(objectArray.getObjectId())) {
						gcRootIds.add(objectArray.getElements()[i]);
					}
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
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
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
	 * @param dbName
	 */
	public void connect(String dbName) {
		try {
			Class.forName("org.sqlite.JDBC");
			String conName = "jdbc:sqlite:" + dbName;
			conn = DriverManager.getConnection(conName);
			conn.setAutoCommit(false);
			PreparedStatement indexStatement = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD);
			ResultSet set = indexStatement.executeQuery();
			while (set.next()) {
				// System.out.print("connect");
				int key = set.getInt("value");
				int value = set.getInt("instance_id");
				String fieldName = set.getString("field_name");
				indexValueToId.put(key, value, fieldName);
			}
			PreparedStatement classStatement = conn
					.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS);
			ResultSet classResultSet = classStatement.executeQuery();
			while (classResultSet.next()) {
				int classId = classResultSet.getInt("id");
				int classLoaderId1 = classResultSet.getInt("class_load_id");
				String name = classResultSet.getString("name");
				indexClassName.put(classId, name);
				if (name.equals("dalvik.system.PathClassLoader")) {
					classLoaderId = classId;
					System.out.print(name);
				}
				if (classLoaderId != 0) {
					classToClassLoader.put(classId, classLoaderId1);
				} else {
					systemClass.add(classId);
				}
			}
			indexStatement.close();
			set.close();
			classResultSet.close();
			classStatement.close();
			PreparedStatement instanceStatement = conn
					.prepareStatement("select * from "
							+ SQLDOMAIN.TABLE_INSTANCE);
			ResultSet instanceResultSet = instanceStatement.executeQuery();
			while (instanceResultSet.next()) {
				int id = instanceResultSet.getInt("id");
				int classId = instanceResultSet.getInt("class_id");
				instanceToClass.put(id, classId);
				if (classId == classLoaderId) {
					classLoaderInstance = id;
				}
				if (!systemClass.contains(classId)) {
					gcRootInstanceOrigin.put(id, new Instance(id, 0, classId,
							0, null));
				}
			}
			instanceResultSet.close();
			instanceStatement.close();
			System.out.print(gcRootInstanceOrigin.size());
			gcRootIds.addAll(initGCRootInstance(classLoaderInstance,
					gcRootInstanceOrigin, classToClassLoader).keySet());
			System.out.print(gcRootIds.size());
			System.out.print(classToClassLoader.size()+"\n");
			System.out.print(classLoaderInstance);
			System.out.print(classLoaderId);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 建立一个数据库名zieckey.db的连接，如果不存在就在当前目录下创建之

	}

	/**
	 * @param dbName
	 */
	public void createTables(String dbName) {
		try {
			Class.forName("org.sqlite.JDBC");
			// 建立一个数据库名zieckey.db的连接，如果不存在就在当前目录下创建之
			String conName = "jdbc:sqlite:" + dbName + ".db";
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
	 * @return
	 */
	public boolean hasConnect() {
		return conn != null;
	}

	/**
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
				InstanceTraceItem item = getInstanceTraceItem(id,
						new ArrayList<Integer>(), length);
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

	/**
	 * @param id
	 * @param traceIds
	 * @param originLength
	 * @return
	 */
	public InstanceTraceItem getInstanceTraceItem(int id,
			ArrayList<Integer> traceIds, int originLength) {
		InstanceTraceItem traceItem = new InstanceTraceItem();
		traceItem.setId(id);
		int length = findLengthById(id, new ArrayList<Integer>(), originLength);
		traceItem.setLength(length);
		if (traceIds.contains(id)) {
			return null;
		}
		traceIds.add(id);
		try {
			boolean hasData = false;
			if (indexValueToId.size() > 0) {
				HashMap<Integer, String> item = indexValueToId.get(id);
				if (item != null && item.size() > 0) {
					hasData = true;
					Iterator iterator = item.entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry entry = (Map.Entry) iterator.next();
						int temp_id = (int) entry.getKey();
						traceItem.setFieldName((String) entry.getValue());
						String name = getClassNameForInstance(id);
						if (name != null) {
							if (name.equals("java.lang.ref.FinalizerReference")) {
								return null;
							}
							traceItem.setName(name);
							InstanceTraceItem temp = getInstanceTraceItem(
									temp_id, traceIds, length);
							if (temp != null) {
								traceItem.addTrace(temp);
							}
						}
					}
				}
			} else {
				PreparedStatement state = conn
						.prepareStatement("select instance_id from "
								+ SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD
								+ " where value = " + id);
				ResultSet set = state.executeQuery();
				while (set.next()) {
					hasData = true;
					int temp_id = set.getInt("instance_id");
					String name = getClassNameForInstance(id);
					if (name != null) {
						if (name.equals("java.lang.ref.FinalizerReference")) {
							return null;
						}
						traceItem.setName(name);
						InstanceTraceItem temp = getInstanceTraceItem(temp_id,
								traceIds, length);
						if (temp != null) {
							traceItem.addTrace(temp);
						}
					}

				}
			}

			if (!hasData) {
				PreparedStatement statement = conn
						.prepareStatement("select id from "
								+ SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
								+ " where element_instance_id=" + id);
				ResultSet objSet = statement.executeQuery();
				while (objSet.next()) {
					hasData = true;
					int objId = objSet.getInt("id");
					String name = getClassNameForInstance(id);
					if (name != null) {
						if (name.equals("java.lang.ref.FinalizerReference")) {
							return null;
						}
						traceItem.setName(name);
						InstanceTraceItem temp = getInstanceTraceItem(objId,
								traceIds, length);
						if (temp != null) {
							traceItem.addTrace(temp);
							;
						}
					}
					statement.close();
					objSet.close();
				}
				if (!hasData) {
					hasData = true;
					PreparedStatement classState = conn
							.prepareStatement("select * from "
									+ SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD
									+ " where value=" + id);
					ResultSet classSet = classState.executeQuery();
					while (classSet.next()) {
						String className = getClassNameForClass(classSet
								.getInt("class_id"));
						traceItem.setName(className);
						traceItem
								.setFieldName(classSet.getString("field_name"));
					}
					classSet.close();
					classState.close();
				}
				if (!hasData) {
					String className = getClassNameForClass(id);
					traceItem.setName(className);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (traceItem.getTraceItems().size() == 0
				&& !gcRootIds.contains(traceItem.getId())) {
			return null;
		}
		if (traceItem.getName() != null) {
			return traceItem;
		}
		return null;
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
			// System.out.print(totalSize);
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
				if (systemClass.contains(classId)) {
					PreparedStatement statement = conn
							.prepareStatement("delete from "
									+ SQLDOMAIN.TABLE_CLASS_INSTANCE_FIELD
									+ " where instance_id = " + id);
					statement.execute();
					statement.close();
					// System.out.print(classId);
				}
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

	public void initInstanceOOM() {
		System.out.print(gcRootInstanceOrigin.size() + "\n");
		System.out.print(instances.size() + "\n");
		System.out.print(systemClass.size() + "\n");
		// oomInstance.putAll(finalizersInstances);
		// Iterator iterator = finalizersInstances.keySet().iterator();
		// System.out.print(oomInstance.size() + "\n");
		// System.out.print(finalizersInstances.size() + "\n");
		// while (iterator.hasNext()) {
		// int instanceId = (int) iterator.next();
		// checkInstanceForRef(instanceId,new ArrayList<Integer>());
		// }
		// System.out.print(oomInstance.size() + "\n");
		// Iterator iterator2 = oomInstance.keySet().iterator();
		// while (iterator2.hasNext()) {
		// int id = (int) iterator2.next();
		// Instance instance = oomInstance.get(id);
		// checkInstanceForOOM(instance, new ArrayList<Integer>());
		// }
		// System.out.print(oomInstance2.size() + "\n");
		// for (Integer key : oomInstance2.keySet()) {
		// if (!oomInstane.containsKey(key)) {
		// oomInstance.put(key, oomInstance2.get(key));
		// }
		// }
		// System.out.print(oomInstance.size());
		// // oomInstance.putAll(oomInstance2);
		// initInstanceDB(gcRootIds);
		gcRootInstance = initGCRootInstance(classLoaderInstance,
				gcRootInstanceOrigin, classToClassLoader);
		gcRootIds.addAll(gcRootInstance.keySet());
		System.out.print(gcRootInstance.size() + "\n");
		HashMap<Integer, Instance> gcLinkInstance = initGCLinkInstance(
				gcRootInstance, instances);
		System.out.print(gcLinkInstance.size() + "\n");
		initInstanceDB(gcLinkInstance);
	}

	//
	// private void checkInstanceForRef(int instanceId,ArrayList<Integer> trace)
	// {
	// if (trace.contains(instanceId)) {
	// return;
	// }
	// trace.add(instanceId);
	// if (oomInstance.containsKey(instanceId)) {
	// Instance temp = oomInstance.get(instanceId);
	// oomInstance.remove(instanceId);
	// unOomInstance.put(instanceId, temp);
	// if (classFiled.containsKey(temp.getClassId())) {
	// List<InstanceField> list1 = classFiled.get(temp.getClassId());
	// Iterator<InstanceField> iterator2 = list1.iterator();
	// ArrayList types = new ArrayList();
	// ArrayList sizes = new ArrayList();
	// ArrayList fieldNames = new ArrayList();
	// while (iterator2.hasNext()) {
	// InstanceField field = iterator2.next();
	// BasicType type = field.getType();
	// types.add(type.type);
	// sizes.add(type.size);
	// }
	// Iterator iterator = types.iterator();
	// Iterator iterator1 = sizes.iterator();
	// byte[] bytes = temp.getInstanceFieldData();
	// int i = 0;
	// while (i < bytes.length && iterator.hasNext()) {
	// int type = (Integer) iterator.next();
	// int size = (Integer) iterator1.next();
	// if (type == 2) {
	// int value = Utils.byteArrayToInt(bytes, i);
	// if (value != 0 &&
	// (oomInstance.containsKey(value)||systemInstance.containsKey(value))) {
	// checkInstanceForRef(value,trace);
	// }
	// }
	// i = i + size;
	// }
	// }
	// }else if (systemInstance.containsKey(instanceId)) {
	// Instance temp = systemInstance.get(instanceId);
	// systemInstance.remove(instanceId);
	// unOomInstance.put(instanceId, temp);
	// if (classFiled.containsKey(temp.getClassId())) {
	// List<InstanceField> list1 = classFiled.get(temp.getClassId());
	// Iterator<InstanceField> iterator2 = list1.iterator();
	// ArrayList types = new ArrayList();
	// ArrayList sizes = new ArrayList();
	// ArrayList fieldNames = new ArrayList();
	// while (iterator2.hasNext()) {
	// InstanceField field = iterator2.next();
	// BasicType type = field.getType();
	// types.add(type.type);
	// sizes.add(type.size);
	// }
	// Iterator iterator = types.iterator();
	// Iterator iterator1 = sizes.iterator();
	// byte[] bytes = temp.getInstanceFieldData();
	// int i = 0;
	// while (i < bytes.length && iterator.hasNext()) {
	// int type = (Integer) iterator.next();
	// int size = (Integer) iterator1.next();
	// if (type == 2) {
	// int value = Utils.byteArrayToInt(bytes, i);
	// if (value != 0 &&
	// (oomInstance.containsKey(value)||systemInstance.containsKey(value))) {
	// checkInstanceForRef(value,trace);
	// }
	// }
	// i = i + size;
	// }
	// }
	// }
	// try {
	// PreparedStatement indexObjArr = conn
	// .prepareStatement("select * from "
	// + SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
	// + " where id = " + instanceId);
	// ResultSet indexObjSet = indexObjArr.executeQuery();
	// while (indexObjSet.next()) {
	// System.out.print("array");
	// checkInstanceForRef(indexObjSet.getInt("element_instance_id"),trace);
	// }
	// indexObjSet.close();
	// indexObjArr.close();
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// private void checkInstanceForOOM(Instance instance,
	// ArrayList<Integer> traces) {
	// if (traces.contains(instance.getObjectId())) {
	// return;
	// }
	// traces.add(instance.getObjectId());
	// // Instance temp = oomInstance.get(instanceId);
	// if (classFiled.containsKey(instance.getClassId())) {
	// List<InstanceField> list1 = classFiled.get(instance.getClassId());
	// Iterator<InstanceField> iterator2 = list1.iterator();
	// ArrayList types = new ArrayList();
	// ArrayList sizes = new ArrayList();
	// ArrayList fieldNames = new ArrayList();
	// while (iterator2.hasNext()) {
	// InstanceField field = iterator2.next();
	// BasicType type = field.getType();
	// types.add(type.type);
	// sizes.add(type.size);
	// }
	// Iterator iterator = types.iterator();
	// Iterator iterator1 = sizes.iterator();
	// byte[] bytes = instance.getInstanceFieldData();
	// int i = 0;
	// while (i < bytes.length && iterator.hasNext()) {
	// int type = (Integer) iterator.next();
	// int size = (Integer) iterator1.next();
	// if (type == 2) {
	// int value = Utils.byteArrayToInt(bytes, i);
	// if (value != 0) {
	// if(traces.contains(value)){
	// if (systemInstance.containsKey(instance.getObjectId())) {
	// systemInstance.remove(instance.getObjectId());
	// }
	// return;
	// }
	// if (value==1137572496) {
	// System.out.print(instance.getObjectId()+"/n");
	// }
	// if (!oomInstance.containsKey(value)
	// &&
	// !oomInstance2.containsKey(value)&&!finalizersInstances.containsKey(value))
	// {
	// if (systemInstance.containsKey(value)) {
	// Instance temp = systemInstance.get(value);
	// oomInstance2.put(value, temp);
	// checkInstanceForOOM(temp, traces);
	// } else if (unOomInstance.containsKey(value)) {
	// Instance temp = unOomInstance.get(value);
	// unOomInstance.remove(value);
	// oomInstance2.put(value, temp);
	// checkInstanceForOOM(temp, traces);
	// }
	// }
	// }
	// }
	// i = i + size;
	// }
	// }
	// }
	//
	private HashMap<Integer, Instance> initGCRootInstance(int classloaderId,
			HashMap<Integer, Instance> origin,
			HashMap<Integer, Integer> classToClassloader) {
		HashMap<Integer, Instance> result = new HashMap<Integer, Instance>();
		Iterator iterator = origin.keySet().iterator();
		while (iterator.hasNext()) {
			int id = (int) iterator.next();
			Instance instance = origin.get(id);
			if (classToClassloader.get(instance.getClassId()) != null
					&& classToClassloader.get(instance.getClassId()) == classloaderId
					&& !result.containsKey(instance.getObjectId())) {
				result.put(instance.getObjectId(), instance);
			}
		}
		return result;
	}

	private HashMap<Integer, Instance> initGCLinkInstance(
			HashMap<Integer, Instance> origin, HashMap<Integer, Instance> all) {
		HashMap<Integer, Instance> result = new HashMap<Integer, Instance>();
		Iterator iterator = origin.keySet().iterator();
		while (iterator.hasNext()) {
			int key = (int) iterator.next();
			Instance instance = origin.get(key);
			List<InstanceField> instanceFields = classFiled.get(instance
					.getClassId());
			Iterator<InstanceField> fieldIterator = instanceFields.iterator();
			ArrayList types = new ArrayList();
			ArrayList sizes = new ArrayList();
			while (fieldIterator.hasNext()) {
				InstanceField field = fieldIterator.next();
				BasicType type = field.getType();
				types.add(type.type);
				sizes.add(type.size);
			}
			Iterator typeIterator = types.iterator();
			Iterator iterator1 = sizes.iterator();
			byte[] bytes = instance.getInstanceFieldData();
			int i = 0;
			while (i < bytes.length && typeIterator.hasNext()) {
				int typeItem = (Integer) typeIterator.next();
				int size = (Integer) iterator1.next();
				if (typeItem == 2) {
					int value = Utils.byteArrayToInt(bytes, i);
					if (value != 0 && !result.containsKey(value)
							&& all.containsKey(value)) {
						result.put(value, all.get(value));
					}
				}
				i = i + size;
			}
		}
		return result;
	}

	private void initInstanceDB(HashMap<Integer, Instance> oomMap) {
		Iterator iterator = oomMap.keySet().iterator();
		System.out.print(oomMap.size());
		while (iterator.hasNext()) {
			int instanceLength = 0;
			int key = (int) iterator.next();
			Instance instance = oomMap.get(key);
			try {

				instanceToClass.put(instance.getObjectId(),
						instance.getClassId());
				List<InstanceField> list1 = classFiled.get(instance
						.getClassId());
				Iterator<InstanceField> iterator2 = list1.iterator();
				ArrayList types = new ArrayList();
				ArrayList sizes = new ArrayList();
				ArrayList fieldNames = new ArrayList();
				while (iterator2.hasNext()) {
					InstanceField field = iterator2.next();
					BasicType type = field.getType();
					types.add(type.type);
					sizes.add(type.size);
					int id = field.getFieldNameId();
					PreparedStatement stringStatement;
					stringStatement = conn.prepareStatement(new StringBuilder(
							"select * from ").append(SQLDOMAIN.TABLE_STRING)
							.append(" where id=").append(id).append(";")
							.toString());
					ResultSet stringSet = stringStatement.executeQuery();
					if (stringSet.next()) {
						fieldNames.add(stringSet.getString("value"));
					}
					stringStatement.close();
					stringSet.close();
				}
				Iterator typeIterator = types.iterator();
				Iterator iterator1 = sizes.iterator();
				Iterator nameIterator = fieldNames.iterator();
				byte[] bytes = instance.getInstanceFieldData();
				int i = 0;
				while (i < bytes.length && typeIterator.hasNext()) {
					int typeItem = (Integer) typeIterator.next();
					int size = (Integer) iterator1.next();
					String fieldName = (String) nameIterator.next();
					if (typeItem == 2) {
						int value = Utils.byteArrayToInt(bytes, i);
						if (value != 0 && oomMap.containsKey(value)) {
							PreparedStatement statement = conn
									.prepareStatement(new StringBuilder(
											"insert into ")
											.append(SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD)
											.append(" (instance_id,type,value,field_name) values (")
											.append(instance.getObjectId())
											.append(",").append(typeItem)
											.append(",").append("?")
											.append(",").append("?")
											.append(");").toString());
							statement.setInt(1, value);
							statement.setString(2, fieldName);
							statement.executeUpdate();
							statement.close();
							indexValueToId.put(value, instance.getObjectId(),
									fieldName);
							// }
						}
					} else {
						instanceLength += size;
					}
					i = i + size;
				}
				StringBuffer buffer = new StringBuffer().append("insert into ")
						.append(SQLDOMAIN.TABLE_INSTANCE).append(" values (")
						.append(instance.getObjectId()).append(",")
						.append(instance.getClassId()).append(",")
						.append(instance.getLength()).append(",")
						.append(instanceLength).append(");");
				PreparedStatement insertState = conn.prepareStatement(buffer
						.toString());
				insertState.executeUpdate();
				insertState.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initInstanceDB(HashSet<Integer> list) {
		Iterator iterator = list.iterator();

		// System.out.print(list.size());
		while (iterator.hasNext()) {
			int instanceLength = 0;
			int key = (int) iterator.next();
			Instance instance = instances.get(key);
			if (instance != null) {
				// if (referenceClass.contains(instance.getClassId())) {
				// continue;
				// }
				try {

					instanceToClass.put(instance.getObjectId(),
							instance.getClassId());
					List<InstanceField> list1 = classFiled.get(instance
							.getClassId());
					Iterator<InstanceField> iterator2 = list1.iterator();
					ArrayList types = new ArrayList();
					ArrayList sizes = new ArrayList();
					ArrayList fieldNames = new ArrayList();
					while (iterator2.hasNext()) {
						InstanceField field = iterator2.next();
						BasicType type = field.getType();
						types.add(type.type);
						sizes.add(type.size);
						int id = field.getFieldNameId();
						PreparedStatement stringStatement;
						stringStatement = conn
								.prepareStatement(new StringBuilder(
										"select * from ")
										.append(SQLDOMAIN.TABLE_STRING)
										.append(" where id=").append(id)
										.append(";").toString());
						ResultSet stringSet = stringStatement.executeQuery();
						if (stringSet.next()) {
							fieldNames.add(stringSet.getString("value"));
						}
						stringStatement.close();
						stringSet.close();
					}
					Iterator typeIterator = types.iterator();
					Iterator iterator1 = sizes.iterator();
					Iterator nameIterator = fieldNames.iterator();
					byte[] bytes = instance.getInstanceFieldData();
					int i = 0;
					while (i < bytes.length && typeIterator.hasNext()) {
						int typeItem = (Integer) typeIterator.next();
						int size = (Integer) iterator1.next();
						String fieldName = (String) nameIterator.next();
						if (typeItem == 2) {
							int value = Utils.byteArrayToInt(bytes, i);
							if (value != 0 && list.contains(value)) {
								PreparedStatement statement = conn
										.prepareStatement(new StringBuilder(
												"insert into ")
												.append(SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD)
												.append(" (instance_id,type,value,field_name) values (")
												.append(instance.getObjectId())
												.append(",").append(typeItem)
												.append(",").append("?")
												.append(",").append("?")
												.append(");").toString());
								statement.setInt(1, value);
								statement.setString(2, fieldName);
								statement.executeUpdate();
								statement.close();
								indexValueToId.put(value,
										instance.getObjectId(), fieldName);
								// }
							}
						} else {
							instanceLength += size;
						}
						i = i + size;
					}
					StringBuffer buffer = new StringBuffer()
							.append("insert into ")
							.append(SQLDOMAIN.TABLE_INSTANCE)
							.append(" values (").append(instance.getObjectId())
							.append(",").append(instance.getClassId())
							.append(",").append(instance.getLength())
							.append(",").append(instanceLength).append(");");
					PreparedStatement insertState = conn
							.prepareStatement(buffer.toString());
					insertState.executeUpdate();
					insertState.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private void analyzerInstance() {

	}
	// interface
}
