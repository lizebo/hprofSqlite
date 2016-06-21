package com.whaley.hprof;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SHProceManager {
	public static void createHprof(String ip,String appName,String hprofPath) throws IOException{
		String path = "createHprof.sh";
		ProcessBuilder builder = new ProcessBuilder("sh", path,ip,appName,hprofPath);
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(builder.start().getInputStream()));
			String line;

			while ((line=br.readLine())!=null) {
			   System.out.println(line);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

}
