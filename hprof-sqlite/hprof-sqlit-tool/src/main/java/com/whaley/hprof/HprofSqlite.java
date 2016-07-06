package com.whaley.hprof;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import com.badoo.hprof.library.HprofReader;
import com.whaley.hprof.sqlitemanager.SqliteManager;
import com.whaley.hprof.sqlitemanager.model.InstanceTraceItem;


public class HprofSqlite {
    public static void main(String[] args) {
        try {
        	CmdProceManager.createHprof("172.16.106.38", "com.moretv.android", "E:\\android-sdk\\platform-tools");
//            Scanner sc = new Scanner(System.in);
//            sc.useDelimiter("\r\n");
//
//            System.out.println("请输入文件路径：");
//
//            String filePath = sc.nextLine();
////            File inFile = new File("E:\\dump\\com.moretv.hprof");
//            File inFile = new File(filePath);
//            InputStream in = new BufferedInputStream(new FileInputStream(inFile));
//            SQLDataProcessor processor = new SQLDataProcessor();
//            HprofReader reader = new HprofReader(in, processor);
//            while (reader.hasNext()) {
//                reader.next();
//            }
////            SqliteManager.getInstance().deleteFinali();
//            SqliteManager.getInstance().initClassLength();
//            SqliteManager.getInstance().getTotalSize();
//            while (true){
//                System.out.println("请选择要进行的操作：1、查找可能的内存泄漏。2、查找某个类的内存占用。3、查找某个类实例的引用链。4、退出\n");
//                int type = Integer.parseInt(sc.nextLine());
//                switch (type){
//                    case 1:
//                        SqliteManager.getInstance().findHeapMax();
//                        break;
//                    case 2:
//                        System.out.println("请输入类名：");
////                        while (!sc.hasNextInt()){
//                            String name = sc.nextLine();
//                            SqliteManager.getInstance().findTraceByName(name);
////                        }
//                        break;
//                    case 3:
//                        System.out.print("请输入类名：");
//                        String name2 = sc.nextLine();
//                        ArrayList<Integer> list = (ArrayList) SqliteManager.getInstance().getInstanceForClass(name2);
////                        Iterator<Integer> iterator = list.iterator();
//                        for (int i = 1;i<=list.size();i++){
//                            System.out.println(i+":"+list.get(i-1)+"\n");
//                        }
//                        if (list.size()>0){
//                            System.out.print("请输入要查询的实例序号：");
//                            int temp = Integer.parseInt(sc.nextLine());
//                            if (temp<=list.size()){
////                                SqliteManager.getInstance().printTrance(list.get(temp-1));
//                                InstanceTraceItem instanceTraceItem = SqliteManager.getInstance().getInstanceTrace(list.get(temp-1));
//                                instanceTraceItem.print();
//                            }
//                        }else {
//                            System.out.print("该类没有实例");
//                        }
////                        while (iterator.hasNext()){
////                            int instanceId = iterator.next();
////                            System.out.print();
////                        }
//                        break;
//                    case 4:
//                        SqliteManager.getInstance().commit();
//                        System.exit(0);
//                        break;
//                }
//            }
//            SqliteManager.getInstance().findHeapMax();
//            SqliteManager.getInstance().findTraceByName("com.moretv.baseCtrl.Util");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
