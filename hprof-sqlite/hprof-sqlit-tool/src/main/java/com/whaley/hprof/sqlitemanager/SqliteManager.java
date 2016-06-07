package com.whaley.hprof.sqlitemanager;

import com.badoo.hprof.library.Tag;
import com.badoo.hprof.library.heap.HeapTag;
import com.badoo.hprof.library.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hc on 2016/5/20.
 */
public class SqliteManager {
    private static  SqliteManager instance = null;
    Connection conn = null;
    private int totalSize = 0;

    private HashMap<Integer, List<InstanceField>> classFiled;
    private List<Integer> traceIds;
    private int finizeId = 0;
    private HashMap<Integer, List<ConstantField>> classConstantFiled;
    private HashMap<Integer, List<StaticField>> classStaticFiled;
    private HashMap<Integer, Integer> classLength;

    private SqliteManager() {
//        PreparedStatement statement1 = null;
//        PreparedStatement statement2 = null;
//        createTables();
        classFiled = new HashMap<Integer, List<InstanceField>>();
        traceIds = new ArrayList<Integer>();
        classConstantFiled = new HashMap<Integer, List<ConstantField>>();
        classStaticFiled = new HashMap<Integer, List<StaticField>>();
        classLength = new HashMap<Integer, Integer>();
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

    public void insertHeapData(int id, int length) {
//        try {
////            StringBuilder buffer = new StringBuilder("insert into ").append(SQLDOMAIN.TABLE_HEAP).append(" values (").append(id).append(",").append(length).append(");");
//            PreparedStatement statement2 = conn.prepareStatement(buffer.toString());
//            statement2.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    public void insertData(int tag, Record obj) {
        StringBuilder buffer = new StringBuilder();
        PreparedStatement insertState = null;
        int i;
        try {
//            Statement stat = conn.createStatement();
            conn.setAutoCommit(false);
            switch (tag) {
                case Tag.STRING:
                    buffer.append("insert into ").append(SQLDOMAIN.TABLE_STRING).append(" values (").append(((HprofString) obj).getId()).append(",'")
                            .append(((HprofString) obj).getValue()).append("');");
                    insertState = conn.prepareStatement(buffer.toString());
//                    stat.executeUpdate(buffer.toString());
                    insertState.executeUpdate();
//                    conn.commit();
                    if (insertState != null) {
                        insertState.close();
                    }
                    break;
                case HeapTag.CLASS_DUMP:
                    StringBuilder delete = new StringBuilder("DELETE FROM ").append(SQLDOMAIN.TABLE_CLASS).append(" WHERE id=").append(((ClassDefinition) obj).getObjectId()).append(";");
                    PreparedStatement deleteState = conn.prepareStatement(delete.toString());
                    deleteState.executeUpdate();
                    deleteState.close();
                case Tag.LOAD_CLASS:
                    PreparedStatement select = conn.prepareStatement(new StringBuilder("select * from ").append(SQLDOMAIN.TABLE_STRING).append(" where id=")
                            .append(((ClassDefinition) obj).getNameStringId()).append(";").toString());
                    ResultSet rs = select.executeQuery();
                    ClassDefinition temp = (ClassDefinition) obj;
                    int length1 = 0;
                    List<InstanceField> list = new ArrayList<InstanceField>();
                    list.addAll(temp.getInstanceFields());
                    if (temp.getSuperClassObjectId() != 0 && classFiled.get(temp.getSuperClassObjectId()) != null)
                        list.addAll(classFiled.get(temp.getSuperClassObjectId()));
                    classFiled.put(temp.getObjectId(), list);
                    Iterator<ConstantField> constantFieldIterator = temp.getConstantFields().iterator();
                    Iterator<StaticField> staticFieldIterator = temp.getStaticFields().iterator();
                    int cl = 0;
                    i = 0;
                    while (constantFieldIterator.hasNext()) {
                        ConstantField constantField = constantFieldIterator.next();
                        int type = (Integer) constantField.getType().type;
                        if (type == 2) {
                            int value = Utils.byteArrayToInt(constantField.getValue(), i);
                            if (value != 0) {
                                PreparedStatement statement = conn.prepareStatement(new StringBuilder("insert into ")
                                        .append(SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD).append(" (class_id,type,value) values (")
                                        .append(temp.getObjectId()).append(",").append(type).append(",").append("?").append(");").toString());
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
                            int value = Utils.byteArrayToInt(staticField.getValue(), i);
                            if (value != 0) {
                                PreparedStatement statement = conn.prepareStatement(new StringBuilder("insert into ")
                                        .append(SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD).append(" (class_id,type,value) values (")
                                        .append(temp.getObjectId()).append(",").append(type).append(",").append("?").append(");").toString());
                                statement.setInt(1, value);
                                statement.executeUpdate();
                                statement.close();
                            } else {
                                cl += staticField.getValue().length;
                            }
                        }
                        classLength.put(temp.getObjectId(), cl);
                    }
                    if (rs.getString("value").equals("java.lang.ref.FinalizerReference"))
                        finizeId = temp.getObjectId();
                    buffer.append("insert into ").append(SQLDOMAIN.TABLE_CLASS).append(" values (").append(temp.getObjectId()).append(",")
                            .append("'").append(rs.getString("value")).append("'").append(",").append(temp.getSuperClassObjectId()).append(",")
                            .append(temp.getClassLoaderObjectId()).append(",").append(temp.getSignersObjectId()).append(",")
                            .append(temp.getProtectionDomainObjectId()).append(",").append(temp.getInstanceSize()).append(",")
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
                    int instanceLength = 0;
                    List<InstanceField> list1 = classFiled.get(instance.getClassId());
                    Iterator<InstanceField> iterator2 = list1.iterator();
                    ArrayList types = new ArrayList();
                    ArrayList sizes = new ArrayList();
                    while (iterator2.hasNext()) {
                        BasicType type = iterator2.next().getType();
                        types.add(type.type);
                        sizes.add(type.size);
                    }
                    Iterator iterator = types.iterator();
                    Iterator iterator1 = sizes.iterator();
                    byte[] bytes = instance.getInstanceFieldData();
                    i = 0;
                    while (i < bytes.length && iterator.hasNext()) {
                        if (instance.getClassId() == finizeId) {
                            break;
                        }
                        int type = (Integer) iterator.next();
                        int size = (Integer) iterator1.next();
                        if (type == 2) {
                            int value = Utils.byteArrayToInt(bytes, i);
                            if (value != 0) {
                                PreparedStatement statement = conn.prepareStatement(new StringBuilder("insert into ")
                                        .append(SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD).append(" (instance_id,type,value) values (")
                                        .append(instance.getObjectId()).append(",").append(type).append(",").append("?").append(");").toString());
                                statement.setInt(1, value);
                                statement.executeUpdate();
                                statement.close();
//                                }
                            }
                        } else {
                            instanceLength += size;
                        }
                        i = i + size;
                    }
                    buffer.append("insert into ").append(SQLDOMAIN.TABLE_INSTANCE).append(" values (").append(instance.getObjectId()).
                            append(",").append(instance.getClassId()).append(",").append(instance.getLength()).
                            append(",").append(instanceLength).append(");");
                    insertState = conn.prepareStatement(buffer.toString());
                    insertState.executeUpdate();
                    if (insertState != null) {
                        insertState.close();
                    }
                    break;
                case HeapTag.PRIMITIVE_ARRAY_DUMP:
                    buffer.append("insert into ").append(SQLDOMAIN.TABLE_PRIMITIVEARRAY).append(" values(").append(((PrimitiveArray) obj).
                            getObjectId()).append(",").append(((PrimitiveArray) obj).getType().type).append(",").append(((PrimitiveArray) obj).
                            getArrayData().length).append(");");
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
                        StringBuilder buffer1 = new StringBuilder("insert into ").append(SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS).append(" values(").append(objectArray.getObjectId())
                                .append(",").append(objectArray.getElements()[i]).append(",").append(0).append(");");
                        PreparedStatement statement = conn.prepareStatement(buffer1.toString());
                        statement.executeUpdate();
                    }
                    buffer.append("insert into ").append(SQLDOMAIN.TABLE_OBJARRAY).append(" values(").append(objectArray.getObjectId())
                            .append(",").append(objectArray.getElementClassId()).append(",").append(objectArray.getCount()).append(",")
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

    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCycle() {
        try {
            PreparedStatement findFinaLizerInstance = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_INSTANCE);
            ResultSet set = findFinaLizerInstance.executeQuery();
            while (set.next()) {
                int id = set.getInt("id");
                PreparedStatement deleteState = conn.prepareStatement("delete from " + SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD +
                        " where instance_id=" + id);
                deleteState.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            //建立一个数据库名zieckey.db的连接，如果不存在就在当前目录下创建之
            String conName = "jdbc:sqlite:"+dbName +".db";
            conn = DriverManager.getConnection(conName);
            conn.setAutoCommit(false);
            PreparedStatement delStringTable = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_STRING + ";");
            delStringTable.executeUpdate();
            delStringTable.close();
            PreparedStatement createStringTable = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_STRING +
                    " (id int primary key, value varchar(50));");
            createStringTable.executeUpdate();
            createStringTable.close();
            PreparedStatement dropClassTable = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_CLASS + ";");
            dropClassTable.executeUpdate();
            dropClassTable.close();
            PreparedStatement createClassTable = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_CLASS +
                    " (id int primary key,name varchar(50),super_class_id int,class_load_id int," +
                    "signers_id int,protection_domain_id int,instance_size int,length int);");
            createClassTable.executeUpdate();
            createClassTable.close();
            PreparedStatement dropInstanceTable = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_INSTANCE + ";");
            dropInstanceTable.executeUpdate();
            dropInstanceTable.close();
            PreparedStatement createInstanceTable = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_INSTANCE +
                    " (id int primary key,class_id int,data_size int,length int);");
            createInstanceTable.executeUpdate();
            createInstanceTable.close();
            PreparedStatement dropPrimitivearrayTable = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_PRIMITIVEARRAY + ";");
            dropPrimitivearrayTable.executeUpdate();
            dropPrimitivearrayTable.close();
            PreparedStatement createPrimitiverArrayTable = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_PRIMITIVEARRAY +
                    "(id int primary key,type int,length int);");
            createPrimitiverArrayTable.executeUpdate();
            createPrimitiverArrayTable.close();
            PreparedStatement dropObjArrayTable = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_OBJARRAY + ";");
            dropObjArrayTable.executeUpdate();
            dropObjArrayTable.close();
            PreparedStatement createObjArrayTable = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_OBJARRAY +
                    "(id int primary key,element_class_id int,count int,length int);");
            createObjArrayTable.executeUpdate();
            createObjArrayTable.close();
            PreparedStatement dropHeapObjArrayIndex = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS + ";");
            dropHeapObjArrayIndex.executeUpdate();
            dropHeapObjArrayIndex.close();
            PreparedStatement createHeapObjArrayIndex = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS +
                    "(id int,element_instance_id int,int length);");
            createHeapObjArrayIndex.executeUpdate();
            createHeapObjArrayIndex.close();
//            PreparedStatement dropClassInstanceTable = conn.prepareStatement("drop table if exists "+SQLDOMAIN.TABLE_CLASS_INSTANCE_FIELD+";");
//            dropClassInstanceTable.executeUpdate();
//            dropClassInstanceTable.close();
//            PreparedStatement createClassInstanceTable = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_CLASS_INSTANCE_FIELD +
//                    "(class_id int,field_name_id int,type int,size int);");
//            createClassInstanceTable.executeUpdate();
//            createClassInstanceTable.close();
            PreparedStatement dropInstanceFiled = conn.prepareStatement("drop table if exists " + SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD + ";");
            dropInstanceFiled.executeUpdate();
            dropInstanceFiled.close();
            PreparedStatement createInstanceField = conn.prepareStatement("create table " + SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD +
                    "(instance_id int, type int,value)");
            createInstanceField.executeUpdate();
            createInstanceField.close();
            PreparedStatement dropClassFiled = conn.prepareStatement("drop table if exists " + SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD + ";");
            dropClassFiled.executeUpdate();
            dropClassFiled.close();
            PreparedStatement createClassField = conn.prepareStatement("create table " + SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD +
                    "(class_id int, type int,value)");
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

    public void findHeapMax() {
        try {
            PreparedStatement selectMaxState = conn.prepareStatement("select * from table_primitive_array order by length desc limit 3;");
            ResultSet resultSet = selectMaxState.executeQuery();
//            selectMaxState.executeUpdate();
            while (resultSet.next()) {
                System.out.print("可疑内存路径："+"\n");
                System.out.print(BasicType.fromType(resultSet.getInt("type")).getTypeName()+"["+resultSet.getInt("length")+"]"+"\n");
                int id = resultSet.getInt("id");
                printTrance(id);
//                traceIds.clear();
//                printTranceById(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    String getClassNameForInstance(int instance_id) {
        try {
            boolean hasData = false;
            PreparedStatement statement = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_INSTANCE + " where id = " + instance_id);
            ResultSet instanceSet = statement.executeQuery();
            while (instanceSet.next()) {
                hasData = true;
                int class_id = instanceSet.getInt("class_id");
                PreparedStatement classState = conn.prepareStatement("select name from " + SQLDOMAIN.TABLE_CLASS + " where id = " + class_id);
                ResultSet resultSet = classState.executeQuery();
                while (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
            if (!hasData){
                PreparedStatement objArrState = conn.prepareStatement("select * from " +SQLDOMAIN.TABLE_OBJARRAY + " where id = "+instance_id);
                ResultSet resultset = objArrState.executeQuery();
                while (resultset.next()){
                    int class_id = resultset.getInt("element_class_id");
                    PreparedStatement classState = conn.prepareStatement("select name from " + SQLDOMAIN.TABLE_CLASS + " where id = " + class_id);
                    ResultSet resultSet = classState.executeQuery();
                    while (resultSet.next()) {
                        return resultSet.getString("name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List getInstanceForClass(String name){
        ArrayList list = new ArrayList();
        try {
            int classId=0;
            PreparedStatement cls = conn.prepareStatement("select id from " + SQLDOMAIN.TABLE_CLASS + " where name=\"" + name + "\"");
            ResultSet set = cls.executeQuery();
            while (set.next()) {
                classId = set.getInt("id");
            }
            PreparedStatement insForCls = conn.prepareStatement("select id from "+SQLDOMAIN.TABLE_INSTANCE + " where class_id =" + classId);
            ResultSet resultSet = insForCls.executeQuery();
            while (resultSet.next()){
                list.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    String getClassNameForClass(int class_id) {
        try {
            PreparedStatement classState = conn.prepareStatement("select name from " + SQLDOMAIN.TABLE_CLASS + " where id = " + class_id);
            ResultSet resultSet = classState.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printTrance(int id){
        traceIds.clear();
        printTranceById(id,0);
    }

    public InstanceTraceItem getInstanceTrace(int id){
        return getInstanceTraceItem(id);
    }


    void printTranceById(int id,int level) {
        level ++;
        if (traceIds.contains(id)) {
            return;
        }
        traceIds.add(id);
        try {
            boolean hasData = false;
            PreparedStatement state = conn.prepareStatement("select instance_id from " + SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD
                    + " where value = " + id);
            ResultSet set = state.executeQuery();
            while (set.next()) {
                hasData = true;
                int temp_id = set.getInt("instance_id");
                String name = getClassNameForInstance(id);
                if (name != null) {
                    if (name.equals("java.lang.ref.FinalizerReference")) {
                        return;
                    }
                    for (int i = 0;i<level;i++)
                        System.out.print("|----");
                    System.out.print("("+id+")"+name + "["+temp_id+"]"+"\n");
                }
                printTranceById(temp_id,level);
            }
            if (!hasData) {
                PreparedStatement statement = conn.prepareStatement("select id from " + SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
                        + " where element_instance_id=" + id);
                ResultSet objSet = statement.executeQuery();
                while (objSet.next()) {
                    hasData = true;
                    int objId = objSet.getInt("id");
                    String name = getClassNameForInstance(id);
                    if (name != null) {
                        if (name.equals("java.lang.ref.FinalizerReference")) {
                            return;
                        }
                        for (int i = 0;i<level;i++)
                            System.out.print("|----");
                        System.out.print("("+id+")"+name + "["+objId+"]"+"\n");
                    }
                    printTranceById(objId,level);
                    statement.close();
                    objSet.close();
                }
                if (!hasData) {
                    PreparedStatement classState = conn.prepareStatement("select class_id from " + SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD
                            + " where value=" + id);
                    ResultSet classSet = classState.executeQuery();
                    while (classSet.next()) {
                        String className = getClassNameForClass(classSet.getInt("class_id"));
                        for (int i = 0;i<level;i++)
                            System.out.print("|----");
                        if (className != null) {
                            System.out.print( "("+id+")" + className+ "\n");
                        }
                    }
                    classSet.close();
                    classState.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    InstanceTraceItem getInstanceTraceItem(int id) {
        InstanceTraceItem traceItem = new InstanceTraceItem();
        traceItem.setId(id);
        if (traceIds.contains(id)) {
            return null;
        }
        traceIds.add(id);
        try {
            boolean hasData = false;
            PreparedStatement state = conn.prepareStatement("select instance_id from " + SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD
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
                    InstanceTraceItem temp = getInstanceTraceItem(temp_id);
                    if (temp!=null){
                        traceItem.addTrace(temp);;
                    }
                }

            }
            if (!hasData) {
                PreparedStatement statement = conn.prepareStatement("select id from " + SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS
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
                        InstanceTraceItem temp = getInstanceTraceItem(objId);
                        if (temp!=null){
                            traceItem.addTrace(temp);;
                        }
//                        for (int i = 0;i<level;i++)
//                            System.out.print("|----");
//                        System.out.print("("+id+")"+name + "["+objId+"]"+"\n");
                    }
                    statement.close();
                    objSet.close();
                }
                if (!hasData) {
                    PreparedStatement classState = conn.prepareStatement("select class_id from " + SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD
                            + " where value=" + id);
                    ResultSet classSet = classState.executeQuery();
                    while (classSet.next()) {
                        String className = getClassNameForClass(classSet.getInt("class_id"));
                        traceItem.setName(className);
//                        for (int i = 0;i<level;i++)
//                            System.out.print("|----");
//                        if (className != null) {
//                            System.out.print( "("+id+")" + className+ "\n");
//                        }
                    }
                    classSet.close();
                    classState.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (traceItem.getName()!=null)
            return traceItem;
        return null;
    }

    private int getClassLength(int classId) {
        int length = 0;
        try {
            PreparedStatement classInfo = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS + " where id = " + classId);
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
                    PreparedStatement update = conn.prepareStatement("update " + SQLDOMAIN.TABLE_CLASS + " set length=" + length +
                            " where id = " + classId);
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

    public void initClassLength() {
        PreparedStatement classInfo = null;
        try {
            classInfo = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS);
            ResultSet set = classInfo.executeQuery();
            while (set.next()) {
                int id = set.getInt("id");
                getClassLength(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getTotalSize() {
        if (totalSize > 0) {
            return totalSize;
        }
        try {
//            PreparedStatement calculateClassSize = conn.prepareStatement("select length from "+ SQLDOMAIN.TABLE_CLASS + " where length>0");
//            ResultSet classResult = calculateClassSize.executeQuery();
//            while (classResult.next()){
//                totalSize+= classResult.getInt("length");
//            }
//            PreparedStatement calculateInstanceSize = conn.prepareStatement("select length from " + SQLDOMAIN.TABLE_INSTANCE + " where length >0");
//            ResultSet instanceResult = calculateInstanceSize.executeQuery();
//            while (instanceResult.next()){
//                totalSize+= instanceResult.getInt("length");
//            }
            PreparedStatement calculatePrimitiveSize = conn.prepareStatement("select length from " + SQLDOMAIN.TABLE_PRIMITIVEARRAY + " where length >0");
            ResultSet primitiveResult = calculatePrimitiveSize.executeQuery();
            while (primitiveResult.next()) {
                totalSize += primitiveResult.getInt("length");
            }
//            System.out.print(totalSize);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalSize;
    }

    private int initInstanceLength(int id) {
        int length = 0;
        try {
            boolean hasInstance = false;
            PreparedStatement instanceState = conn.prepareStatement(new StringBuilder("select * from ").append(SQLDOMAIN.TABLE_INSTANCE)
                    .append(" where id = ").append(id).append(";").toString());
            ResultSet instanceResult = instanceState.executeQuery();
            while (instanceResult.next()) {
                hasInstance = true;
                length += instanceResult.getInt("length");
                if (length > 0) {
                    return length;
                }
                int classId = instanceResult.getInt("class_id");
                List<InstanceField> fields = classFiled.get(classId);
//                length+=classLength.get(classId);
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
                        PreparedStatement subInstance = conn.prepareStatement(new StringBuilder("select value from ")
                                .append(SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD).append(" where instance_id =").append(id)
                                .append(";").toString());
                        ResultSet subInstanceSet = subInstance.executeQuery();
                        if (!subInstanceSet.next()) {
                            PreparedStatement subObjArrayState = conn.prepareStatement(new StringBuilder("select length from ")
                                    .append(SQLDOMAIN.TABLE_OBJARRAY).append(" where id = ").append(id).append(";").toString());
                            ResultSet subObjArraySet = subObjArrayState.executeQuery();
                            if (!subObjArraySet.next()) {
                                PreparedStatement subPrimitiveArrayState = conn.prepareStatement(new StringBuilder("select * from ")
                                        .append(SQLDOMAIN.TABLE_PRIMITIVEARRAY).append(" where id = ").append(id).append(";")
                                        .toString());
                                ResultSet subPriArraySet = subPrimitiveArrayState.executeQuery();
                                while (subPriArraySet.next()) {
                                    int type = subPriArraySet.getInt("type");
                                    int count = subPriArraySet.getInt("count");
                                    BasicType basicType = BasicType.fromType(type);
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
                            length += initInstanceLength(subInstanceSet.getInt("value"));
                            while (subInstanceSet.next()) {
                                length += initInstanceLength(subInstanceSet.getInt("value"));
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

    private int findLengthById(int classId, List<Integer> traceIds, int level, ArrayList<TraceItem> result) {
        int length = 0;
        traceIds.add(classId);
        level++;
        try {
            PreparedStatement cls = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS + " where id = " + classId);
            ResultSet clsSet = cls.executeQuery();
            boolean hasData = false;
            while (clsSet.next()) {
                hasData = true;
                length += clsSet.getInt("length");
                PreparedStatement insForClass = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_INSTANCE + " where class_id=" + classId);
                ResultSet instances = insForClass.executeQuery();
                while (instances.next()) {
                    int id = instances.getInt("id");
                    length += findLengthById(id, traceIds, level, result);
                }
                insForClass.close();
                instances.close();
                PreparedStatement staticAndConsForClass = conn.prepareStatement("select * from " + SQLDOMAIN.INDEW_CLASS_CONS_STATIC_FIELD + " where class_id=" + classId);
                ResultSet sAndCSet = staticAndConsForClass.executeQuery();
                while (sAndCSet.next()) {
                    int id = sAndCSet.getInt("value");
                    length += findLengthById(id, traceIds, level, result);
                }
                staticAndConsForClass.close();
                sAndCSet.close();
                if (result.size() < level) {
                    result.add(new TraceItem(clsSet.getString("name"), length));
                } else {
                    int temp = result.get(level - 1).getValue();
                    if (length > temp) {
                        result.remove(level - 1);
                        result.add(level - 1, new TraceItem(clsSet.getString("name"), length));
                    }
                }
//                System.out.print(clsSet.getString("name")+" length="+length+" level="+level+" percent:"+ (float)length/totalSize+"\n");
            }
            cls.close();
            clsSet.close();
            if (!hasData) {
                PreparedStatement instance = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_INSTANCE + " where id = " + classId);
                ResultSet insInfo = instance.executeQuery();
                while (insInfo.next()) {
                    hasData = true;
                    PreparedStatement insForClass = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS + " where id = " +
                            insInfo.getInt("class_id"));
                    ResultSet inForClassSet = insForClass.executeQuery();
                    length += insInfo.getInt("length");
                    PreparedStatement insForIns = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_INDEX_INSTANCE_FIELD +
                            " where instance_id=" + classId);
                    ResultSet insSet = insForIns.executeQuery();
                    while (insSet.next()) {
                        int id = insSet.getInt("value");
                        if (traceIds.contains(id)) {
                            return length;
                        } else {
                            length += findLengthById(id, traceIds, level, result);
                        }
                    }
                    insForIns.close();
                    insSet.close();
                    while (inForClassSet.next()) {
//                        for (int i = 0;i<level;i++)
//                            System.out.print("  ");
                        if (result.size() < level) {
                            result.add(new TraceItem(inForClassSet.getString("name"), length));
                        } else {
                            int temp = result.get(level - 1).getValue();
                            if (length > temp) {
                                result.remove(level - 1);
                                result.add(level - 1, new TraceItem(inForClassSet.getString("name"), length));
                            }
                        }
//                        System.out.print(inForClassSet.getString("name")+" length="+length+" level="+level+" percent:"+ (float)length/totalSize+"\n");
                    }
                    insForClass.close();
                    inForClassSet.close();
                }
                instance.close();
                insInfo.close();
                if (!hasData) {
                    PreparedStatement objArrState = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_OBJARRAY + " where id = " + classId);
                    ResultSet objSet = objArrState.executeQuery();
                    while (objSet.next()) {
                        hasData = true;
                        PreparedStatement objForClass = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_CLASS + " where id = " +
                                objSet.getInt("element_class_id"));
                        ResultSet objForClassSet = objForClass.executeQuery();
                        PreparedStatement objIndexState = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_INDEX_HEAP_OBJARR_CLASS +
                                " where id=" + classId);
                        ResultSet objIndexSet = objIndexState.executeQuery();
                        while (objIndexSet.next()) {
                            length += findLengthById(objIndexSet.getInt("element_instance_id"), traceIds, level, result);
                        }
                        while (objForClassSet.next()) {
//                            for (int i = 0;i<level;i++)
//                                System.out.print("  ");
                            if (result.size() < level) {
                                result.add(new TraceItem(objForClassSet.getString("name"), length));
                            } else {
                                int temp = result.get(level - 1).getValue();
                                if (length > temp) {
                                    result.remove(level - 1);
                                    result.add(level - 1, new TraceItem(objForClassSet.getString("name"), length));
                                }
                            }
//                            System.out.print(objForClassSet.getString("name")+"[]"+" length="+length+" level="+level+" percent:"+ (float)length/totalSize+"\n");
                        }
                        objForClass.close();
                        objForClassSet.close();
                    }
                    objArrState.close();
                    objSet.close();
                    if (!hasData) {
                        hasData = true;
                        PreparedStatement priArrState = conn.prepareStatement("select * from " + SQLDOMAIN.TABLE_PRIMITIVEARRAY + " where id = " + classId);
                        ResultSet set = priArrState.executeQuery();
                        while (set.next()) {
                            length += set.getInt("length");
//                            for (int i = 0;i<level;i++)
//                                System.out.print("  ");
                            if (result.size() < level) {
                                result.add(new TraceItem(BasicType.fromType(set.getInt("type")).getTypeName(), length));
                            } else {
                                int temp = result.get(level - 1).getValue();
                                if (length > temp) {
                                    result.remove(level - 1);
                                    result.add(level - 1, new TraceItem(BasicType.fromType(set.getInt("type")).getTypeName() + "[]", length));
                                }
                            }
//                            System.out.print(BasicType.fromType(set.getInt("type")).getTypeName()+"[]"+ " length="+length+" level="+level+" percent:"+ (float)length/totalSize +"\n");
                        }
                        priArrState.close();
                        set.close();
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return length;
    }

    public void findTraceByName(String name) {
        try {
            PreparedStatement cls = conn.prepareStatement("select id from " + SQLDOMAIN.TABLE_CLASS + " where name=\"" + name + "\"");
            ResultSet set = cls.executeQuery();
            while (set.next()) {
                ArrayList<TraceItem> result = new ArrayList<TraceItem>();
                findLengthById(set.getInt("id"), new ArrayList<Integer>(), 0, result);
                for (int i = 0; i < result.size(); i++)
                    System.out.print(result.get(i).getName() + " length=" + result.get(i).getValue() + " level=" + i + " percent:" + (float) result.get(i).getValue() / totalSize + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
