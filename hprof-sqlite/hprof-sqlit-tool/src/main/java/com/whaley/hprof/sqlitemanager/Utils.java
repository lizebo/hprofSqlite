package com.whaley.hprof.sqlitemanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by hc on 2016/5/26.
 */
public class Utils {
	public static int byteArrayToInt(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	public static int byteArrayToShort(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 2; i++) {
			int shift = (2 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	public static ArrayList<String> convertStreamToString(InputStream is) {
		ArrayList<String> result = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = null;

		try {

			while ((line = reader.readLine()) != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(line);
				result.add(sb.toString());
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				is.close();

			} catch (IOException e) {

				e.printStackTrace();

			}

		}

		return result;

	}

}
