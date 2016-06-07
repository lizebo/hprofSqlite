package com.whaley.hprof.sqlitemanager;

/**
 * Created by hc on 2016/5/26.
 */
public class Utils {
    public static int byteArrayToInt(byte[] b, int offset) {
        int value= 0;
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    public static int byteArrayToShort(byte[] b, int offset) {
        int value= 0;
        for (int i = 0; i < 2; i++) {
            int shift= (2 - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
}
